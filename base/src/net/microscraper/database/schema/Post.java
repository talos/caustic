package net.microscraper.database.schema;

public class Post {
	public final static String RESOURCE = "post";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	
	public final String name;
	public final String value;
	
	public Post(String _name, String _value) {
		name = _name;
		value = _value;
	}
}
