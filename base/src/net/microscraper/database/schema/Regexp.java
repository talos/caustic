package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class Regexp {
	public static class Model extends AbstractModel {
		public static final String KEY = "regexp";
	
		public static final String REGEXP = "regexp";
		public static final String[] ATTRIBUTES = { REGEXP };
		
		public final Relationship[] relationships = { };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}