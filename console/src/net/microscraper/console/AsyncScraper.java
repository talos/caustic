package net.microscraper.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;
import net.microscraper.util.StringUtils;

public class AsyncScraper implements Loggable, Runnable {
	private final ExecutorService executor;
	private final MultiLog log = new MultiLog();
	private final HttpBrowser browser;
	private final Instruction instruction;
	private final Database database;
	private final Map<String, String> input;
	private final String source;
	
	/**
	 * List of the names of successful scrapers.
	 */
	private final List<String> successes = new ArrayList<String>();
	
	/**
	 * List of reasons for failed scrapers.
	 */
	private final List<String> failures = new ArrayList<String>();
	
	private void logDidntStart(DatabaseException e) {
		log.i("Couldn't start scraping of "  + StringUtils.quote(instruction) +
				" with input " + StringUtils.quote(input.toString()) + "." + 
				" There was a database error: " + e);
	}

	private void logNominalCompletion() {
		log.i("Completed scraping of " + StringUtils.quote(instruction) +
				" with input " + StringUtils.quote(input.toString()) + "." + 
				" There were " + successes.size() + " successful instructions, " + 
				" and " + failures.size() + " failed instructions.  The failures" +
				" were as follows: " + failures.toString());
	}
	
	private void logIncomplete(List<CallableScraper> stuckScrapers, List<String> missingTags) {
		log.i("Couldn't complete scraping of " + StringUtils.quote(instruction) +
				" with input " + StringUtils.quote(input.toString()) + " because" + 
				" these " + StringUtils.quote(stuckScrapers) + " were missing" +
				" the tags " + StringUtils.quote(missingTags) +
				" There were " + successes.size() + " successful instructions," + 
				" and " + failures.size() + " failed instructions.  The failures" +
				" were as follows: " + failures.toString());
	}
	
	private void logTermination(List<CallableScraper> stuckScrapers, List<String> missingTags,
			Throwable terminatingThrowable,
			List<Runnable> notYetRun) {
		log.i("Terminated scraping of " + StringUtils.quote(instruction) +
				" with input " + StringUtils.quote(input.toString()) + " because" + 
				" of " + terminatingThrowable + ". There were " + notYetRun.size() + 
				" instructions in the queue." +
				" " + StringUtils.quote(stuckScrapers) + " were missing the tags" +
				" " + StringUtils.quote(missingTags) + " " +
				" There were " + successes.size() + " successful instructions," + 
				" and " + failures.size() + " failed instructions.  The failures" +
				" were as follows: " + failures.toString());
	}
	
	
	/**
	 * Invoke a {@link List} of {@link CallableScraper}s.
	 * @param toInvoke
	 * @param lastMissingTags
	 * @return A {@link String} .
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void invoke(List<CallableScraper> toInvoke,
							List<String> lastMissingTags) {		
		List<CallableScraper> invokeNext = new ArrayList<CallableScraper>();
		List<CallableScraper> reinvoke = new ArrayList<CallableScraper>();
		List<String> nowMissingTags = new ArrayList<String>();
		Throwable prematureTermination = null;
		
		try {
			List<Future<ScraperResult>> invoked = executor.invokeAll(toInvoke);
			
			for(Future<ScraperResult> futureResult : invoked) {
				// get the result -- invokeAll() guarantees it's available.
				ScraperResult result = futureResult.get();
				if(result.isSuccess()) {
					successes.add(result.getName());
					
					for(Scraper child : result.getChildren()) {
						invokeNext.add(new CallableScraper(child));
					}
				} else if (result.isMissingTags()) {
					Scraper scraperToRetry = result.getScraperToRetry();
					
					nowMissingTags.addAll(Arrays.asList(result.getMissingTags()));
					
					if(scraperToRetry.isStuck()) {
						reinvoke.add(new CallableScraper(scraperToRetry));
					} else {
						invokeNext.add(new CallableScraper(scraperToRetry));
					}
				} else {
					failures.add(result.getFailedBecause());
				}
			}
		} catch(ExecutionException e) {
			e.printStackTrace();
			prematureTermination = e.getCause();
		} catch(InterruptedException e) {
			prematureTermination = e;
		}
		
		if(prematureTermination != null) {
			List<Runnable> notYetRun = executor.shutdownNow();
			logTermination(reinvoke, nowMissingTags, prematureTermination, notYetRun);
		} else {
			
			invokeNext.addAll(reinvoke); // put stuck scrapers at end
			
			boolean missingTagsAreSame = 
					nowMissingTags.containsAll(lastMissingTags) &&
					lastMissingTags.containsAll(nowMissingTags);
			
			if(invokeNext.size() == 0) {
				logNominalCompletion();
				executor.shutdown();
			// continue invoking iff there were some non-stuck scrapers, or
			// the missing tags changed.
			} else if(invokeNext.size() > reinvoke.size() || !missingTagsAreSame) {
				invoke(invokeNext, nowMissingTags);
			} else {
				logIncomplete(reinvoke, nowMissingTags);
				executor.shutdown();
			}
		}
	}
	
	public AsyncScraper(Instruction instruction, Map<String, String> input, Database database,
			String source, HttpBrowser browser, int nThreads) {
		this.executor = Executors.newFixedThreadPool(nThreads);
		this.instruction = instruction;
		this.input = input;
		this.browser = browser;
		this.database = database;
		this.source = source;
	}

	@Override
	public void run() {
		log.i("Scraping " + StringUtils.quote(instruction));
		
		try {
			DatabaseView view = database.newView();
			for(Map.Entry<String, String> entry : input.entrySet()) {
				view.put(entry.getKey(), entry.getValue());
			}

			// the starting callable scraper.
			CallableScraper cScraper = new CallableScraper(new Scraper(instruction, view, source, browser));
			List<String> noMissingTags = Collections.emptyList();
			invoke(Arrays.asList(new CallableScraper[] { cScraper }), noMissingTags);
			
		} catch(DatabaseException e) {
			logDidntStart(e);
		}
	}
	
	public void kill() {
		executor.shutdownNow();
	}

	@Override
	public void register(Logger logger) {
		log.register(logger);
	}
}
