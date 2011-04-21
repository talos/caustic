package net.microscraper.client;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.database.Database.DatabaseException;
import net.microscraper.database.Database;
import net.microscraper.database.Execution;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Default;

public class Client {
	private Log _log = new Log();
	private Regexp _regexp;
	private JSON _json;
	private Browser _browser;
	private Publisher _publisher;
	private Database _db = new Database();
	
	private static Client instance = new Client();
	public static final Log log = instance._log;
	public static final Regexp regexp = instance._regexp;
	public static final JSON json = instance._json;
	public static final Browser browser = instance._browser;
	public static final Publisher publisher = instance._publisher;
	public static final Database db = instance._db;
	
	private Client() { }
	public static Client get(Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Logger[] loggers, Publisher publisher) {
		
		instance._browser = browser;
		instance._regexp = regexp;
		instance._json = json;
		instance._publisher = publisher;
		for(int i = 0; i < loggers.length ; i ++) {
			instance._log.register(loggers[i]);
		}
		
		return instance;
	}
	
	public Execution.Root scrape(String json_url, Reference ref, Default[] extra_defaults)
					throws MicroScraperClientException {
		//ResultRoot root = new ResultRoot();
		String raw_obj;
		try {
			Client.log.i("Scraping '" + ref.toString() + "' from JSON loaded from " + json_url);
			
			raw_obj = browser.load(json_url);
				log.i("Raw scraping JSON: " + raw_obj);
		} catch(BrowserException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(InterruptedException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		}
		
		Execution.Root root = new Execution.Root();
		try {
			for(int i = 0 ; i < extra_defaults.length ; i ++) {
				root.call(extra_defaults[i]);
			}
			db.inflate(json.getTokener(raw_obj).nextValue());
			root.call(db.get(ref));
			return root;
		}  catch(JSONInterfaceException e) {
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
