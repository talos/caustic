package net.microscraper.client;

import net.microscraper.client.Resources.ResourceException;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Status;
import net.microscraper.resources.definitions.Scraper.ScraperResult;

/**
 * A microscraper Client allows for scraping from microscraper json.
 * @author john
 *
 */
public class Client {
	public final Log log;
	public final Interfaces.Regexp regexp;
	public final Interfaces.JSON json;
	public final Browser browser;
	public final Publisher publisher;
	//public final Resources resources = new Resources(this);
		
	public Client(Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Interfaces.Logger[] loggers, Publisher publisher) {
		this.browser = browser;
		this.regexp = regexp;
		this.json = json;
		this.publisher = publisher;
		this.log = new Log();
		for(int i = 0; i < loggers.length ; i ++) {
			log.register(loggers[i]);
		}
	}
	
	public void scrape(Interfaces.JSON.Object json, Reference ref, ScraperResult[] extraVariables) {
		try {
			Resources resources = new Resources(this, json);
			Resource resource = resources.get(ref);
			
			// Loop while we're in progress, provided the number of missing variables is changing.
			Status curStatus = new Status();
			Status lastStatus;
			do {
				lastStatus = curStatus;
				curStatus = resource.execute(extraVariables);
			} while(curStatus.hasProgressedSince(lastStatus));
		} catch(ResourceException e) {
			log.e(e);
		} catch (ExecutionFatality e) {
			log.e(e);
		}
	}
	
	public static class MicroScraperClientException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8899853760225376402L;

		public MicroScraperClientException(Throwable e) { super(e); }
	}
}
