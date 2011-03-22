package net.microscraper.database.schema;

public final class Data {	
	public final static String NAME = "data";
	public final static String SCRAPERS = "scrapers";
	public final static String DEFAULTS = "defaults";
	
	public final Scraper[] scrapers;
	public final Default[] defaults;
	
	public Data(Scraper[] _scrapers, Default[] _defaults) {
		scrapers = _scrapers;
		defaults = _defaults;
	}
}
