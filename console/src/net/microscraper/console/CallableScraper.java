package net.microscraper.console;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.log.Logger;
import net.microscraper.util.StringUtils;

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
	private final Logger log;
	
	public CallableScraper(Scraper scraper, ScraperRunner runner, Logger log) {
		this.log = log;
		this.scraper = scraper;
		this.runner = runner;
	}
	
	@Override
	public ScraperResult call() throws InterruptedException, IOException {
		log.i("Scraping " + scraper);
		ScraperResult result = scraper.scrape();
		if(result.isSuccess()) { // submit children
			Scraper[] children = result.getChildren();
			log.i("Scraper " + StringUtils.quote(scraper) + " is successful, adding "
					+ children.length + " children to queue.");
			for(Scraper child : children) {
				runner.submit(child);
			}
		} else if(result.isMissingTags()) { // resubmit if missing tags
			log.i("Scraper " + scraper + " is missing tags " + 
					StringUtils.quoteJoin(result.getMissingTags(), ", ") + ", trying " +
					"again later.");
			runner.resubmit(scraper);
		} else {
			log.i("Scraper " + scraper + " failed: " + 
					StringUtils.quote(result.getFailedBecause()));
		}
		
		return result;
	}
}
