package net.microscraper.database.schema;

public final class Scraper {	
	public final static String RESOURCE = "scraper";
	
	public final static String REGEXP = "regexp";
	public final static String MATCH_NUMBER = "match_number";
	public final static String WEB_PAGES = "web_pages";
	public final static String SOURCE_SCRAPERS = "source_scrapers";
	
	public final Regexp regexp;
	public final Integer match_number;
	public final WebPage[] web_pages;
	public final Scraper[] source_scrapers;
	
	public Scraper(Regexp _regexp, Integer _match_number,
				WebPage[] _web_pages, Scraper[] _source_scrapers) {
		regexp = _regexp;
		match_number = _match_number;
		web_pages = _web_pages;
		source_scrapers = _source_scrapers;
	}
	
	/**
	 * 
	 * @param results A subset of a Data's results set that would be accessible to the Scraper.
	 * @return An array of 
	 */
	/*public String[] scrape(Hashtable variables) {
		String source;
		if(source == null && web_pages.size() > 0) {
			source = (String) web_pages.elementAt(0).load(variables);
			if(source != null)
				web_pages.removeElementAt(0);
		}
		if(source == null && source_scrapers.size() > 0) {
			source = (String) source_scrapers.elementAt(0).scrape(variables);
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
	}*/
}
