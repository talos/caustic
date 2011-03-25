package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;

public abstract class AbstractHeader extends AbstractModel {
	public static String NAME = "name";
	public static String VALUE = "value";
	public static String[] ATTRIBUTES = { NAME, VALUE };
	
	public static Relationship[] RELATIONSHIPS = {};
	
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }

	public static class Post extends AbstractHeader {
		public static String KEY = "post";
		protected String _key() { return KEY; }
	}
	public static class Header extends AbstractHeader {
		public static String KEY = "header";
		protected String _key() { return KEY; }
	}
	public static class Cookie extends AbstractHeader {
		public static String KEY = "cookie";
		protected String _key() { return KEY; }
	}
}