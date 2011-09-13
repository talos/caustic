package net.microscraper.console;

import java.io.IOException;
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
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseView;
import net.microscraper.instruction.Instruction;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;
import net.microscraper.util.StringUtils;

public class AsyncScraper implements Loggable, Callable<AsyncScraperStatus> {
	private final ExecutorService executor;
	private final List<Future<ScraperResult>> submitted =
			Collections.synchronizedList(new ArrayList<Future<ScraperResult>>());
	private final MultiLog log = new MultiLog();
	private final Scraper rootScraper;

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
	
	private Future<ScraperResult> submit(CallableScraper scraper) {
		Future<ScraperResult> future = executor.submit(scraper);
		synchronized(submitted) {
			submitted.add(future);
		}
		return future;
	}
	
	private boolean workThroughSubmitList() {
		boolean allSubmittedDone = true;
		allSubmittedDone = true;
		synchronized(submitted) {
			for(Future<ScraperResult> future : submitted) {
				if(future.isDone() == false) {
					allSubmittedDone = false;
				} else {
					try {
						ScraperResult result = future.get();
						
						if(result.isSuccess()) {  // submit children if success
							Scraper[] scraperChildren = result.getChildren();
							logSuccess(scraperChildren.length);
							
							for(Scraper child : scraperChildren) {
								executor.submit(new CallableScraper(child, log));
							}
							
						} else if(result.isMissingTags()) { 
							String[] missingTags = result.getMissingTags();
							
							if(scraper.isStuck()) {// do not resubmit if missing tags and stuck
								logStuck(missingTags);
							} else {// resubmit if missing tags but not stuck
								logMissingTags(missingTags);
								executor.submit(this);
							}
							
						} else {
							logFailure(result.getFailedBecause());
						}
					} catch(ExecutionException e) {
						
					}
				}
			}
		}
	}
	
	public AsyncScraper(Instruction instruction, DatabaseView input,
			String source, int nThreads) {
		this.executor = Executors.newFixedThreadPool(nThreads);
		rootScraper = new Scraper(instruction, input, source);
	}
	
	public AsyncScraperStatus getStatus() {
		int numSuccess = 0;
		int numMissingTags = 0;
		int numFailed = 0;
		int numCrashed = 0;
		int numWaiting = 0;
		synchronized(submitted) {
			for(Future<ScraperResult> future : submitted) {
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
	
	public void join() throws InterruptedException {
		boolean allSubmittedDone = false;
		while(allSubmittedDone == false) {
			allSubmittedDone = true;
			synchronized(submitted) {
				for(Future<ScraperResult> future : submitted) {
					if(future.isDone() == false) {
						allSubmittedDone = false;
						break;
					}
				}
			}
			Thread.sleep(500);
		}

		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.DAYS);
	}

	public AsyncScraperStatus call() throws IOException, InterruptedException {
		log.i("Scraping " + rootScraper);
		
		CallableScraper rootCallable = new CallableScraper(rootScraper);
		
		executor.submit(rootCallable);
		
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
