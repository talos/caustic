package net.microscraper.client;

import java.util.Vector;

/**
 * An {@link Executor} implementation that creates a new
 * thread for each {@link Scraper}.
 * @author talos
 *
 */
class AsyncExecutor implements Executor {	
	private Vector stuckScrapers = new Vector();
	private final ScraperThread[] threadPool;
	
	private void resubmitStuckScrapers() {
		synchronized(stuckScrapers) {
			Scraper[] stuckScrapersAry = new Scraper[stuckScrapers.size()];
			stuckScrapers.copyInto(stuckScrapersAry);
			stuckScrapers.clear();
			submit(stuckScrapersAry);
		}
	}
	
	public AsyncExecutor(int nThreads) {
		threadPool = new ScraperThread[nThreads];
		for(int i = 0 ; i < nThreads ; i ++) {
			threadPool[i] = new ScraperThread();
			threadPool[i].start();
		}
	}
	
	public void resubmit(Scraper scraper) {
		if(scraper.isStuck()) {
			synchronized(stuckScrapers) {
				stuckScrapers.add(scraper);
			}
		} else {
			submit(new Scraper[] { scraper});
		}
	}
	
	public void submit(Scraper[] scrapers) {
		// start the scrapers
		for(int i = 0 ; i < scrapers.length ; i ++) {
			Scraper scraper = scrapers[i];
			
			// find the least occupied scraper thread
			ScraperThread leastOccupiedThread = null;
			for(int j = 0 ; j < threadPool.length ; j ++) {
				ScraperThread thread = threadPool[j];
				int queueSize = thread.getQueueSize();
				
				// submit it right away if queue is empty
				if(queueSize == 0 || leastOccupiedThread == null) {
					leastOccupiedThread = thread;
					break;
				// if the previously least occupied thread has a greater
				// queue size, do not use it.
				} else if(leastOccupiedThread.getQueueSize() > queueSize) {
					leastOccupiedThread = thread;
				}
			}
			leastOccupiedThread.addScraper(scraper);
		}
	}
	
	public void interrupt() {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			threadPool[i].interrupt();
		}
	}
	
	public void join() throws InterruptedException {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			ScraperThread thread = threadPool[i];
			// wait for queue to work itself out
			while(thread.getQueueSize() > 0) {
				Thread.sleep(100);
			}
			thread.finish();
			thread.join();
		}
	}
}
