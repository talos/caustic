package net.microscraper.client;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.Reference;
import net.microscraper.database.Result;
import net.microscraper.database.ResultRoot;
import net.microscraper.database.schema.Default;

public class Client {
	public final Log log = new Log();
	public final Regexp regexp;
	public final JSON json;
	public final Browser browser;
	public final Publisher publisher;
	private static Client instance;
	
	private Client (Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Logger[] loggers, Publisher publisher) {
		this.browser = browser;
		this.regexp = regexp;
		this.json = json;
		this.publisher = publisher;
		for(int i = 0; i < loggers.length ; i ++) {
			log.register(loggers[i]);
		}
	}
	
	public static Client initialize (Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Logger[] loggers, Publisher publisher) {
		if(instance != null)
			return instance;
		instance = new Client(browser, regexp, json, loggers, publisher);
		return instance;
	}
	
	public static Client context () {
		if(instance == null)
			throw new IllegalStateException("The client has not been initialized.");
		else
			return instance;
	}
	
	public Result[] scrape(String json_url, Reference ref, Default[] extra_defaults)
					throws MicroScraperClientException {
		try {
			ResultRoot root = new ResultRoot();
			
			log.i("Scraping based off of object loaded from " + json_url);
			
			String raw_obj = browser.load(json_url, root);
			log.i("Raw scraping object: " + raw_obj);
			
			for(int i = 0 ; i < extra_defaults.length ; i ++) {
				extra_defaults[i].getValue(root);
			}
			
			Database db = new Database(json.getTokener(raw_obj).nextValue());
			return db.get(ref).getValue(root);
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
		} catch(InstantiationException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(IllegalAccessException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(TemplateException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(MissingVariable e) {
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
