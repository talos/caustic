package net.microscraper.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.microscraper.client.Scraper;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.instruction.Instruction;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;

public class AsyncScraper implements Loggable, Callable<AsyncScraperStatus> {
	private final ExecutorService executor;
	private final List<Future<Scraper[]>> submitted =
			Collections.synchronizedList(new ArrayList<Future<Scraper[]>>());
	private final MultiLog log = new MultiLog();
	private final Scraper rootScraper;

	/*
	private void logSuccess(Scraper scraper, int numChildren) {
		log.i("Scraper " + StringUtils.quote(scraper) + " is successful, adding "
				+ numChildren + " children to queue.");
	}
	
	private void logMissingTags(Scraper scraper, String[] missingTags) {
		log.i("Scraper " + scraper + " is missing tags " + 
				StringUtils.quoteJoin(missingTags, ", ") +
				", trying again later.");
	}
	
	private void logStuck(Scraper scraper, String[] missingTags) {
		log.i("Scraper " + scraper + " is stuck on tags " + 
				StringUtils.quoteJoin(missingTags, "."));
	}
	
	private void logFailure(Scraper scraper, String failedBecause) {
		log.i("Scraper " + scraper + " failed: " + 
				StringUtils.quote(failedBecause));
	}
	
	private Future<Scraper[]> submit(CallableScraper scraper) {
		Future<Scraper[]> future = executor.submit(scraper);
		synchronized(submitted) {
			submitted.add(future);
		}
		return future;
	}
	*/
	private boolean workThroughSubmitList() throws ExecutionException, InterruptedException {
		boolean allSubmittedDone = true;
		allSubmittedDone = true;
		
		List<CallableScraper> tryAgainLater = new ArrayList<CallableScraper>();
		List<CallableScraper> tryNow = new ArrayList<CallableScraper>();
		
		synchronized(submitted) {
			for(Future<Scraper[]> future : submitted) {
				if(future.isDone() == false) {
					allSubmittedDone = false;
				} else {
					Scraper[] scrapers = future.get();
					for(Scraper scraper : scrapers) {
						if(scraper.isStuck()) {
							tryAgainLater.add(new CallableScraper(scraper));
						} else {
							tryNow.add(new CallableScraper(scraper));
						}
					}
				}
			}
		}
		
		executor.invokeAll(tryNow);
		executor.invokeAll(tryAgainLater);
		
	}
	
	public AsyncScraper(Instruction instruction, DatabaseView input,
			String source, int nThreads) {
		this.executor = Executors.newFixedThreadPool(nThreads);
		rootScraper = new Scraper(instruction, input, source);
	}
	
	/*
	public AsyncScraperStatus getStatus() {
		int numSuccess = 0;
		int numMissingTags = 0;
		int numFailed = 0;
		int numCrashed = 0;
		int numWaiting = 0;
		synchronized(submitted) {
			for(Future<Scraper[]> future : submitted) {
				if(future.isDone() == false) {
					numWaiting++;
				} else {
					try {
						ScraperResult result = future.get();
						if(result.isSuccess()) {
							numSuccess++;
						} else if(result.isMissingTags()) {
							numMissingTags++;
						} else {
							numFailed++;
						}
					} catch(ExecutionException e) {
						e.printStackTrace();
						numCrashed++;
					} catch(InterruptedException e) {
						numCrashed++;
					}
				}
			}
		}
		return new AsyncScraperStatus(numSuccess, numMissingTags, numFailed, numCrashed, numWaiting);
	}
	*/

	public AsyncScraperStatus call() throws InterruptedException, DatabaseException {
		log.i("Scraping " + rootScraper);
		
		CallableScraper rootCallableScraper = new CallableScraper(rootScraper);
		
		executor.submit(rootCallableScraper);
		
		// monitor
		

		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.DAYS);
		
	}
	
	public void kill() {
		executor.shutdownNow();
	}

	@Override
	public void register(Logger logger) {
		log.register(logger);
	}
}
