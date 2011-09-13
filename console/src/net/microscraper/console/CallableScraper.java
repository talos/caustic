package net.microscraper.console;

import java.util.concurrent.Callable;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseException;

/**
 * A {@link Callable} wrapper for {@link Scraper}, that calls {@link Scraper#scrape()}
 * within {@link Callable#call()}.
 * @author talos
 *
 */
public class CallableScraper implements Callable<Scraper[]> {
	private final Scraper scraper;
	
	public CallableScraper(Scraper scraper) {
		this.scraper = scraper;
	}

	@Override
	public Scraper[] call() throws InterruptedException, DatabaseException {
		ScraperResult result = scraper.scrape();
		if(result.isSuccess()) {
			return result.getChildren();
		} else if(result.isMissingTags()) {
			return new Scraper[] { scraper };
		} else {
			return new Scraper[] {};
		}
	}
}
