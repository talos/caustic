package net.microscraper.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.microscraper.client.ScraperResult;

public class ScraperExecutor {
	private final ExecutorService executor;
	private final List<Future<ScraperResult>> submitted =
			Collections.synchronizedList(new ArrayList<Future<ScraperResult>>());
	
	public ScraperExecutor(int nThreads) {
		this.executor = Executors.newFixedThreadPool(nThreads);
	}
	
	public Future<ScraperResult> submit(CallableScraper scraper) {
		Future<ScraperResult> future = executor.submit(scraper);
		synchronized(submitted) {
			submitted.add(future);
		}
		return future;
	}
	
	public String getStatusLine() {
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
						numCrashed++;
					} catch(InterruptedException e) {
						numCrashed++;
					}
				}
			}
		}
		return formatStatusLine(numSuccess, numMissingTags, numFailed, numCrashed, numWaiting);
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
	
	public static String formatStatusLine(int numSuccess, int numMissingTags,
			int numFailed, int numCrashed, int numWaiting) {
		return numSuccess + " successful, " + numMissingTags + " missing tags, " +
			numFailed + " failed, " + numCrashed + " crashed, " +
			numWaiting + " not yet executed.";
	}
	
	public void kill() {
		executor.shutdownNow();
	}
}
