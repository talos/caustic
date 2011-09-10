package net.microscraper.console;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;

/**
 * A {@link Callable} wrapper for {@link Scraper} that adds children
 * scrapers onto the {@link ScraperRunner} queue, and resubmits if
 * the scraping was stopped by missing tags.
 * @author talos
 *
 */
public class CallableScraper implements Callable<ScraperResult> {
	private final Scraper scraper;
	private final ScraperRunner runner;
	
	public CallableScraper(Scraper scraper, ScraperRunner runner) {
		this.scraper = scraper;
		this.runner = runner;
	}
	
	@Override
	public ScraperResult call() throws InterruptedException, IOException {
		ScraperResult result = scraper.scrape();
		if(result.isSuccess()) { // submit children
			Scraper[] children = result.getChildren();
			for(int i = 0 ; i < children.length ; i ++) {
				runner.submit(children[i]);
			}
		} else if(result.isMissingTags()) { // resubmit if missing tags
			runner.resubmit(scraper);
		}
		
		return result;
	}
}
