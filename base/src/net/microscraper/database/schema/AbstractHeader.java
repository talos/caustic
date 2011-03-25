package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class AbstractHeader {
	protected static abstract class AbstractHeaderModel extends AbstractModel {
		public static final String NAME = "name";
		public static final String VALUE = "value";
		public static final String[] ATTRIBUTES = { NAME, VALUE };
		
		public final Relationship[] relationships = {};
		
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	
	}
	public static class Post {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "post";
			protected String _key() { return KEY; }
		}
	}
	public static class Header {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "header";
			protected String _key() { return KEY; }
		}
	}
	public static class Cookie {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "cookie";
			protected String _key() { return KEY; }
		}
	}
}