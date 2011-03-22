package net.microscraper.database.schema;

public class Cookie {
	public final static String RESOURCE = "cookie";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	
	public final String name;
	public final String value;
	
	public Cookie(String _name, String _value) {
		name = _name;
		value = _value;
	}
}
