package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;


public class Scraper {
	public static class Model extends AbstractModel {
		public static final String KEY = "scraper";
	
		public static final String REGEXP = "regexp";
		public static final String MATCH_NUMBER = "match_number";
		public static final String PUBLISH = "publish";
		public static final String[] ATTRIBUTES = { REGEXP, MATCH_NUMBER, PUBLISH };
		
		public static final String WEB_PAGES = "web_pages";
		public static final String SOURCE_SCRAPERS = "source_scrapers";
		public final Relationship web_pages = new Relationship( WEB_PAGES, new WebPage.Model());
		public final Relationship source_scrapers = new Relationship( SOURCE_SCRAPERS, new Scraper.Model());
		public final Relationship[] relationships = { web_pages, source_scrapers };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}