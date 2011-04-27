package net.microscraper.client;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.database.Database.DatabaseException;
import net.microscraper.database.Database;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Reference;
import net.microscraper.database.Resource;

public class Client {
	private static Client instance = new Client();
	public static Log log = new Log();
	public static Regexp regexp;
	public static JSON json;
	public static Browser browser;
	public static Publisher publisher;
	public static Database db = new Database();
	
	private Client() { }
	public static Client get(Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Logger[] loggers, Publisher publisher) {
		Client.browser = browser;
		Client.regexp = regexp;
		Client.json = json;
		Client.publisher = publisher;
		for(int i = 0; i < loggers.length ; i ++) {
			Client.log.register(loggers[i]);
		}
		
		return instance;
	}
	
	public void scrape(String json_url, Reference ref, Variables extraVariables)
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
		
		try {
			//root.addVariables(extraVariables);
			db.inflate(json.getTokener(raw_obj).nextValue());
			Resource resource = db.get(ref);
			
			// Loop while we're in progress.
			Status curStatus = Status.IN_PROGRESS;
			while(curStatus == Status.IN_PROGRESS) {
				curStatus = resource.execute(extraVariables);
			}
		}  catch(JSONInterfaceException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(DatabaseException e) {
			log.e(e);
			throw new MicroScraperClientException(e);
		} catch(FatalExecutionException e) {
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
