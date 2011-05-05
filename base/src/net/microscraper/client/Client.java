package net.microscraper.client;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.database.Database.DatabaseException;
import net.microscraper.database.Database;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Reference;
import net.microscraper.database.Resource;
import net.microscraper.database.Status;

public class Client {
	//private static Client instance = new Client();
	public static Log log;
	public static Regexp regexp;
	public static JSON json;
	public static Browser browser;
	public static Publisher publisher;
	//public static Database db = new Database();
	
	private static boolean initialized = false;
	
	private Client() { }
	public static void initialize(Browser browser, Interfaces.Regexp regexp,
			Interfaces.JSON json, Logger[] loggers, Publisher publisher) {
		Client.browser = browser;
		Client.regexp = regexp;
		Client.json = json;
		Client.publisher = publisher;
		Client.log = new Log();
		for(int i = 0; i < loggers.length ; i ++) {
			log.register(loggers[i]);
		}
		initialized = true;
	}
	
	public static void reset() {
		initialized = false;
	}
	
	public static void scrape(JSON.Object json, Reference ref, Variables extraVariables) {
		if(!initialized)
			throw new IllegalStateException("Scraper not initialized.");
		
		try {
			Database db = new Database();
			db.inflate(json);
			Resource resource = db.get(ref);
			
			// Loop while we're in progress, provided the number of missing variables is changing.
			Status curStatus = new Status();
			Status lastStatus;
			do {
				lastStatus = curStatus;
				curStatus = resource.execute(extraVariables);
			} while(curStatus.hasProgressedSince(lastStatus));
		} catch(DatabaseException e) {
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
