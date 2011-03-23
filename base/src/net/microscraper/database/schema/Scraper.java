package net.microscraper.database.schema;

public final class Scraper {	
	public final static String RESOURCE = "scraper";
	
	public final static String REGEXP = "regexp";
	public final static String MATCH_NUMBER = "match_number";
	public final static String WEB_PAGES = "web_pages";
	public final static String SOURCE_SCRAPERS = "source_scrapers";
	
	public final String regexp;
	public final Integer match_number;
	public final Reference[] web_pages;
	public final Reference[] source_scrapers;
	
	public Scraper(String _regexp, Integer _match_number,
			Reference[] _web_pages, Reference[] _source_scrapers) {
		regexp = _regexp;
		match_number = _match_number;
		web_pages = _web_pages;
		source_scrapers = _source_scrapers;
	}
}
