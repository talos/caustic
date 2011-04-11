package net.microscraper.client;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.Data;
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
	
	public void scrape(String json_url, Publisher publisher) throws MicroScraperClientException {
		scrape(json_url, new Default[] {}, publisher);
	}
	
	public void scrape(String json_url, Default[] extra_defaults, Publisher publisher)
					throws MicroScraperClientException {
		try {
			WebPage json_web_page = new WebPage(json_url);
			log.i("Scraping based off of object loaded from " + json_url);
			
			String raw_obj = browser.load(json_web_page);
			log.i("Raw scraping object: " + raw_obj);
			
			Database db = new Database(json.getTokener(raw_obj).nextValue());
			
			Resource[] datas = db.get(Data.Model.KEY);
			//ResultRoot[] results = new ResultRoot[datas.length];
			for(int i = 0; i < datas.length; i ++) {
				//results[i] = new ResultRoot(publisher);
				ResultSet results = new ResultSet(publisher);
				for(int j = 0 ; j < extra_defaults.length; j ++) {
					try {
						extra_defaults[j].simulate(results);
					} catch (TemplateException e) { // A problem with the mustache template for one of the defaults.  Skip it.
						log.w(e);
					}
				}
				Data data = new Data(datas[i]);
				data.scrape(results);
			}
			
			//return results;
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
