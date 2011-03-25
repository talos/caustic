package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class Default extends AbstractModel {
	public static String KEY = "default";

	public static String VALUE = "value";
	public static String[] ATTRIBUTES = { VALUE };
	
	public static Relationship SUBSTITUTES_FOR = new Relationship( "substitutes_for", new Scraper());
	public static Relationship[] RELATIONSHIPS = { SUBSTITUTES_FOR };
	
	protected String _key() { return KEY; }
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }
}
