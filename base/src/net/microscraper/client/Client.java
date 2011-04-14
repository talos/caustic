package net.microscraper.client;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.WebPage;

public class Client {
	public final Log log = new Log();
	public final Regexp regexp;
	public final JSON json;
	public final Browser browser;
	private static Client instance;
	
	private Client (Browser _browser, Interfaces.Regexp _regexp,
			Interfaces.JSON _json, Logger[] loggers) {
		browser = _browser;
		regexp = _regexp;
		json = _json;
		for(int i = 0; i < loggers.length ; i ++) {
			log.register(loggers[i]);
		}
	}
	
	public static Client initialize (Browser _browser, Interfaces.Regexp _regexp,
			Interfaces.JSON _json, Logger[] loggers) {
		if(instance != null)
			return instance;
		instance = new Client(_browser, _regexp, _json, loggers);
		return instance;
	}
	
	public static Client context () {
		if(instance == null)
			throw new IllegalStateException("The client has not been initialized.");
		else
			return instance;
	}
	
	/*public void scrape(String json_url, Publisher publisher) throws MicroScraperClientException {
		scrape(json_url, new Default[] {}, publisher);
	}*/
	
	//public void scrape(String json_url, Default[] extra_defaults, Publisher publisher)
	public void scrape(String json_url, String model_name, Reference resource, Default[] extra_defaults, Publisher publisher)
					throws MicroScraperClientException {
		try {
			log.i("Scraping based off of object loaded from " + json_url);
			
			String raw_obj = browser.load(json_url);
			log.i("Raw scraping object: " + raw_obj);
			
			Database db = new Database(json.getTokener(raw_obj).nextValue());
			
			for(int j = 0 ; j < extra_defaults.length; j ++) {
				try {
					extra_defaults[j].simulate(results);
				} catch (TemplateException e) { // A problem with the mustache template for one of the defaults.  Skip it.
					log.w(e);
				}
			}
			
		} catch(BrowserException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(InterruptedException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(JSONInterfaceException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(DatabaseException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
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
