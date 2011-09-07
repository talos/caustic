package net.microscraper.console;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.microscraper.client.Scraper;
import net.microscraper.util.Execution;

/**
 * A {@link Callable} wrapper for {@link Scraper}.
 * {@link CallableScraper#call()} is equivalent to
 * {@link Scraper#scrape()}.
 * @author talos
 *
 */
public class CallableScraper implements Callable<Execution[]> {
	private final Scraper scraper;
	
	public CallableScraper(Scraper scraper) {
		this.scraper = scraper;
	}
	
	@Override
	public Execution[] call() throws InterruptedException, IOException {
		return scraper.scrape();
	}
}
