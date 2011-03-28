package net.microscraper.client;

import net.microscraper.client.AbstractResult.Result;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Interfaces.SQL;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.schema.Data;
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
		//sql = _sql;
	}
	
	public static Client initialize (Browser _browser, Interfaces.Regexp _regexp,
			Interfaces.JSON _json, /*Interfaces.SQL _sql,*/ Logger[] loggers) {
		if(instance != null)
			return instance;
		else
			return new Client(_browser, _regexp, /*_sql,*/ _json, loggers);
	}
	
	public static Client context () {
		if(instance == null)
			throw new IllegalStateException("The client has not been initialized.");
		else
			return instance;
	}
	
	public AbstractResult[] scrape(String db_location)
				throws InterruptedException, JSONInterfaceException, DatabaseException, BrowserException {
		WebPage db_web_page = new WebPage(db_location);
		
		Database db = new Database(json.getTokener(browser.load(db_web_page)).nextValue());
		
		Data[] datas = db.datas();
		AbstractResult[] results = new Result[datas.length];
		for(int i = 0; i < datas.length; i ++) {
			results[i] = datas[i].scrape();
		}
		
		return results;
	}
}
