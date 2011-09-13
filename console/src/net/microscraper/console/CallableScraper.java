package net.microscraper.console;

import java.util.concurrent.Callable;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;

/**
 * A {@link Callable} wrapper for {@link Scraper}, that calls {@link Scraper#scrape()}
 * within {@link Callable#call()}.
 * @author talos
 *
 */
public class CallableScraper implements Callable<ScraperResult> {
	private final Scraper scraper;
	
	public CallableScraper(Scraper scraper) {
		this.scraper = scraper;
	}

	@Override
	public ScraperResult call() throws Exception {
		return scraper.scrape();
	}
}
