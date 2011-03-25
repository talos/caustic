package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class Scraper extends AbstractModel {
	public static String KEY = "scraper";

	public static String REGEXP = "regexp";
	public static String MATCH_NUMBER = "match_number";
	public static String PUBLISH = "publish";
	public static String[] ATTRIBUTES = { REGEXP, MATCH_NUMBER, PUBLISH };

	public static Relationship WEB_PAGES = new Relationship( "web_pages", new WebPage());
	public static Relationship SOURCE_SCRAPERS = new Relationship( "source_scrapers", new Scraper());
	public static Relationship[] RELATIONSHIPS = { WEB_PAGES, SOURCE_SCRAPERS };
	
	protected String _key() { return KEY; }
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }
}
