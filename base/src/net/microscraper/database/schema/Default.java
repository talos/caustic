package net.microscraper.database.schema;

public final class Default {
	public final static String RESOURCE = "default";
	public final static String VALUE = "value";
	public final static String SUBSTITUTES_FOR = "substitutes_for";
	
	public final String value;
	public final Reference[] substitutes_for;
	public Default(String _value, Reference[] _substitutes_for) {
		value = _value;
		substitutes_for = _substitutes_for;
	}
}
