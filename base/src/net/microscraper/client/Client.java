package net.microscraper.client;

import net.microscraper.client.AbstractResult.ResultRoot;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Interfaces.SQL;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.WebPage;

public class Client {
	public final Log log = new Log();
	public Regexp regexp;
	public JSON json;
	public SQL sql;
	public Browser browser;
	private static Client instance;
	
	private Client (Browser _browser, Interfaces.Regexp _regexp,
			Interfaces.JSON _json, /*Interfaces.SQL _sql,*/ Logger[] loggers) {
		browser = _browser;
		regexp = _regexp;
		json = _json;
		for(int i = 0; i < loggers.length ; i ++) {
			log.register(loggers[i]);
		}
	}
	
	public static Client initialize (Browser _browser, Interfaces.Regexp _regexp,
			Interfaces.JSON _json, /*Interfaces.SQL _sql,*/ Logger[] loggers) {
		if(instance != null)
			return instance;
		instance = new Client(_browser, _regexp, /*_sql,*/ _json, loggers);
		return instance;
	}
	
	public static Client context () {
		if(instance == null)
			throw new IllegalStateException("The client has not been initialized.");
		else
			return instance;
	}
	
	public AbstractResult[] scrape(String db_location) throws MicroScraperClientException {
		return scrape(db_location, new Default[] {});
	}
	
	public AbstractResult[] scrape(String db_location, Default[] extra_defaults)
					throws MicroScraperClientException {
		try {
			WebPage db_web_page = new WebPage(db_location);
			log.i("Scraping based off of object loaded from " + db_location);
			
			String raw_obj = browser.load(db_web_page);
			log.i("Raw scraping object: " + raw_obj);
			
			Database db = new Database(json.getTokener(raw_obj).nextValue());
			
			Data[] datas = db.datas();
			ResultRoot[] results = new ResultRoot[datas.length];
			for(int i = 0; i < datas.length; i ++) {
				results[i] = new ResultRoot();
				for(int j = 0 ; j < extra_defaults.length; j ++) {
					try {
						extra_defaults[i].simulate(results[i]);
					} catch (TemplateException e) { // A problem with the mustache template for one of the defaults.  Skip it.
						log.w(e);
					}
				}
				datas[i].scrape(results[i]);
			}
			
			return results;
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
