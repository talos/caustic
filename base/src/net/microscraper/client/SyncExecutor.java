package net.microscraper.client;

import net.microscraper.database.DatabaseException;

/**
 * An implementation of {@link Executor} that runs synchronously in
 * the thread that runs {@link #scrape(Scraper[])}.
 * @author talos
 *
 */
class SyncExecutor implements Executor {

	/**
	 * Runs synchronously in-thread.
	 */
	public void submit(Scraper[] scrapers) {
		try {
			for(int i = 0 ; i < scrapers.length ; i ++) {
				scrapers[i].scrape();
			}
		} catch(InterruptedException e) {
			//todo
			e.printStackTrace();
		} catch(DatabaseException e) {
			//todo
			e.printStackTrace();
		}
	}
	
	public void resubmit(Scraper scraper) {
		submit(new Scraper[] { scraper });
	}

	public void interrupt() { }
	
	public void join() { }

}
