package net.microscraper.database.schema;

public final class Data {	
	public final Scraper[] scrapers;
	public final Default[] defaults;
	
	public Data(Scraper[] _scrapers, Default[] _defaults) {
		scrapers = _scrapers;
		defaults = _defaults;
	}
}
