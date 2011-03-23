package net.microscraper.database.schema;

public final class Data {	
	public final static String RESOURCE = "data";
	
	public final static String SCRAPERS = "scrapers";
	public final static String DEFAULTS = "defaults";
	
	public final Reference[] scrapers;
	public final Reference[] defaults;
	
	public Data(Reference[] _scrapers, Reference[] _defaults) {
		scrapers = _scrapers;
		defaults = _defaults;
	}
}
