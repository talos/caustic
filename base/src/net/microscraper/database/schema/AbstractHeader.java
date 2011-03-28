package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class AbstractHeader {
	public final String name;
	public final String value;
	public AbstractHeader(Resource resource, Variables variables) throws TemplateException, MissingVariable {
		name = Mustache.compile(resource.attribute_get(AbstractHeaderModel.NAME), variables);
		value = Mustache.compile(resource.attribute_get(AbstractHeaderModel.VALUE), variables);
	}
	public AbstractHeader(String _name, String _value) {
		name = _name;
		value = _value;
	}
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