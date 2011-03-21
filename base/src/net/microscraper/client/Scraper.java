package net.microscraper.client;

import java.util.Hashtable;
import java.util.Vector;

public class Scraper {
	public final String title;
	
	private final Regexp regexp;
	private final Integer match_number;
	private final Vector web_pages = new Vector();
	private final Vector source_scrapers = new Vector();
	
	public Scraper(String _title, Regexp _regexp, Integer _match_number,
				WebPage[] _web_pages, Scraper[] _source_scrapers) {
		title = _title;
		regexp = _regexp;
		match_number = _match_number;
		Utils.arrayIntoVector(_web_pages, web_pages);
		Utils.arrayIntoVector(_source_scrapers, source_scrapers);
	}
	
	/**
	 * 
	 * @param results A subset of a Data's results set that would be accessible to the Scraper.
	 * @return An array of 
	 */
	public String[] scrape(Data.Results results) {
		String source;
		if(source == null && web_pages.size() > 0) {
			source = (String) web_pages.elementAt(0).load(results);
			if(source != null)
				web_pages.removeElementAt(0);
		}
		if(source == null && source_scrapers.size() > 0) {
			source = (String) source_scrapers.elementAt(0).scrape(results);
			if(source != null)
				source_scrapers.removeElementAt(0);
		}
		return matchInput(source);
	}
	
	private String[] matchInput(String input) {
		if(match_number == null) {
			return regexp.allMatches(input);
		} else {
			return new String[] { regexp.match(input, match_number) };
		}
	}
}
