package net.microscraper.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;

public class ScraperRunner implements Loggable {
	private final ExecutorService executor;
	private final MultiLog log = new MultiLog();
	
	private final List<Future<ScraperResult>> futures = Collections.synchronizedList(
			new ArrayList<Future<ScraperResult>>());
	
	private List<Scraper> resubmit = Collections.synchronizedList(
			new ArrayList<Scraper>());
	
	/**
	 * 
	 * @return <code>true</code> if all futures are done, <code>false</code>
	 * otherwise.
	 */
	private boolean allFuturesDone() {
		synchronized(futures) {
			Iterator<Future<ScraperResult>> iter = futures.iterator();
			while(iter.hasNext()) {
				Future<ScraperResult> future = iter.next();
				if(!future.isDone()) {
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * Re-{@link #submit(Scraper)} everything in {@link #resubmit}.
	 */
	private void retryResubmits() {
		synchronized(resubmit) {
			Iterator<Scraper> iter = resubmit.iterator();
			while(iter.hasNext()) {
				submit(iter.next());
			}
		}
	}
	
	/**
	 * 
	 * @return The number of {@link Scraper}s that are {@link Scraper#isStuck()}
	 * in {@link #resubmit}.
	 */
	private int countStuckResubmits() {
		synchronized(resubmit) {
			int stuckResubmits = 0;
			Iterator<Scraper> iter = resubmit.iterator();
			while(iter.hasNext()) {
				if(iter.next().isStuck()) {
					stuckResubmits++;
				}
			}
			return stuckResubmits;
		}
	}
	
	public ScraperRunner(ExecutorService executor) {
		this.executor = executor;
	}
	
	public void submit(Scraper scraper) {
		synchronized(futures) {
			futures.add(executor.submit(new CallableScraper(scraper, this, log)));
		}
	}
	
	public void resubmit(Scraper scraper) {
		synchronized(resubmit) {
			resubmit.add(scraper);
		}
	}
	
	public void await() throws InterruptedException {
		int prevStuckResubmits;
		int curStuckResubmits = 0;
		do {
			while(allFuturesDone() == false) {
				Thread.sleep(100);
			}
			prevStuckResubmits = curStuckResubmits;
			
			retryResubmits();
			while(allFuturesDone() == false) {
				Thread.sleep(100);
			}
			
			curStuckResubmits = countStuckResubmits();
			
		} while(prevStuckResubmits != curStuckResubmits);
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.DAYS);
	}

	@Override
	public void register(Logger logger) {
		log.register(logger);
	}
}
