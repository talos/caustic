package net.microscraper.database.schema;

public final class Data {	
	public final static String RESOURCE = "data";
	
	public final static String SCRAPERS = "scrapers";
	public final static String DEFAULTS = "defaults";
	
	public final Scraper[] scrapers;
	public final Default[] defaults;
	
	public static String fullName(String creator, String title) {
		return creator + '/' + title;
	}
	
	public Data(Scraper[] _scrapers, Default[] _defaults) {
		scrapers = _scrapers;
		defaults = _defaults;
	}
	
	
}
