package net.microscraper.client;

import java.util.Vector;

import net.microscraper.database.DatabaseException;

final class ScraperThread extends Thread {
	
	private boolean isFinished = false;
	private final Vector scraperQueue = new Vector();
	
	public void run() {
		try {
			while(isFinished == false) {
				Thread.sleep(100);
				while(scraperQueue.size() > 0) {
					Scraper scraper = (Scraper) scraperQueue.elementAt(0);
					scraperQueue.removeElementAt(0);
					scraper.scrape();
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch(DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public void addScraper(Scraper scraper) {
		scraperQueue.addElement(scraper);
	}
	
	public int getQueueSize() {
		return scraperQueue.size();
	}
	
	public void finish() {
		isFinished = true;
	}
}
