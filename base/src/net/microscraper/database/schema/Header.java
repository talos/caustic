package net.microscraper.database.schema;

public class Header {
	public final static String RESOURCE	 = "header";
	public final static String NAME = "name";
	public final static String VALUE  = "value";
	
	public final String name;
	public final String value;
	
	public Header(String _name, String _value) {
		name = _name;
		value = _value;
	}
}
