package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class Default {
	public static class Model extends AbstractModel {
		public static final String KEY = "default";
	
		public static final String VALUE = "value";
		public static final String[] ATTRIBUTES = { VALUE };
		
		public static final String SUBSTITUTES_FOR = "substitutes_for";
		public final Relationship substitutes_for = new Relationship( SUBSTITUTES_FOR, new Scraper.Model());
		public final Relationship[] relationships = { substitutes_for };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}
