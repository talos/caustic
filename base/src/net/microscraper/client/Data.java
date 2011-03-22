package net.microscraper.client;

import net.microscraper.client.interfaces.Regexp;
import net.microscraper.database.schema.*;

public class Data implements Runnable {
	public Data(Cookie[] _cookies, Default[] _defaults, Header[] _headers,
			Post[] _posts, Regexp[] _regexps, Scraper[] _scrapers,
			WebPage[] web_pages) {
		
	}
	
	public void run() {
		collect();
	}
	
	public void collect() {
		
	}
}
