package net.microscraper.console;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.log.Logger;
import net.microscraper.util.StringUtils;

/**
 * A wrapper for {@link Scraper} that submits children (as {@link CallableScraper}s)
 * back to a {@link ExecutorService}.
 * @author talos
 *
 */
public class CallableScraper implements Callable<ScraperResult> {
	private final Scraper scraper;
	private final ScraperExecutor executor;
	private final Logger log;
	
	private void logSuccess(int numChildren) {
		log.i("Scraper " + StringUtils.quote(scraper) + " is successful, adding "
				+ numChildren + " children to queue.");
	}
	
	private void logMissingTags(String[] missingTags) {
		log.i("Scraper " + scraper + " is missing tags " + 
				StringUtils.quoteJoin(missingTags, ", ") +
				", trying again later.");
	}
	
	private void logStuck(String[] missingTags) {
		log.i("Scraper " + scraper + " is stuck on tags " + 
				StringUtils.quoteJoin(missingTags, "."));
	}
	
	private void logFailure(String failedBecause) {
		log.i("Scraper " + scraper + " failed: " + 
				StringUtils.quote(failedBecause));
	}
	
	public CallableScraper(Scraper scraper, ScraperExecutor executor, Logger log) {
		this.log = log;
		this.scraper = scraper;
		this.executor = executor;
	}
	
	public ScraperResult call() throws IOException, InterruptedException {
		log.i("Scraping " + scraper);
		ScraperResult result = scraper.scrape();
		
		if(result.isSuccess()) {  // submit children if success
			Scraper[] scraperChildren = result.getChildren();
			logSuccess(scraperChildren.length);
			
			for(Scraper child : scraperChildren) {
				executor.submit(new CallableScraper(child, executor, log));
			}
			
		} else if(result.isMissingTags()) { 
			String[] missingTags = result.getMissingTags();
			
			if(scraper.isStuck()) {// do not resubmit if missing tags and stuck
				logStuck(missingTags);
			} else {// resubmit if missing tags but not stuck
				logMissingTags(missingTags);
				executor.submit(this);
			}
			
		} else {
			logFailure(result.getFailedBecause());
		}
		return result;
	}
}
