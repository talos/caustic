package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class Regexp extends AbstractModel {
	public static String KEY = "regexp";

	public static String REGEXP = "regexp";
	public static String[] ATTRIBUTES = { REGEXP };
	
	public static Relationship[] RELATIONSHIPS = { };
	
	protected String _key() { return KEY; }
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }
}