package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.client.interfaces.Regexp;
import net.microscraper.database.schema.Cookie;
import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.Reference;
import net.microscraper.database.schema.Scraper;
import net.microscraper.database.schema.WebPage;

public class Client {
	private final Browser browser;
	private final Executions executions = new Executions();
	
	public Client (Browser _browser, Regexp regexInterface) {
		browser = _browser;
	}
	
	public Hashtable evaluate(Collection collection) {
		Hashtable variables = new Hashtable();
		
		fillDefaults(collection, variables);
		runScrapers(collection, variables);
		
		return variables;
	}
	
	private void fillDefaults(Collection collection, Hashtable variables) {		
		Enumeration e = collection.defaults.elements();
		while(e.hasMoreElements()) {
			Default _default = (Default) e.nextElement();
			for(int i = 0; i < _default.substitutes_for.length; i ++) {
				variables.put(_default.substitutes_for[i].title, _default.value);
			}
		}
	}
	
	private void tryScrapers(Collection collection, Hashtable variables) {
		Enumeration e = collection.datas.elements();
		while(e.hasMoreElements()) {
			Data data = (Data) e.nextElement();
			for(int i = 0; i < data.scrapers.length; i ++) {
				if(variables.containsKey(data.scrapers[i].title))
					continue;
				String result = loadScraper(collection, (Scraper) collection.scrapers.get(data.scrapers[i]));
			}
		}
	}
	
	private String loadScraper(Collection collection, Scraper scraper) {
		Enumeration e;
		String[] sources;
		//e = scraper.web_pages.elements();
		Hashtable variables;
		variables = executions.getVariables();
		for(int i = 0; i < scraper.web_pages.length; i++) {
			WebPage source_page = (WebPage) collection.web_pages.get(scraper.web_pages[i]);
			String source = browser.load(source_page);
			runScraper(scraper, source);
		}
		
		e = scraper.source_scrapers.elements();
	}
	
	private String runScraper(Scraper scraper, String source) {
		if(scraper.match_number == null) {
			scraper.regexp
		} else {
			
		}
	}
}
