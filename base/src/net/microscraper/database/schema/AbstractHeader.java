package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Relationships.Relationship;
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
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof AbstractHeader))
			return false;
		AbstractHeader other = (AbstractHeader) obj;
		if(this.name.equals(other.name) && this.value.equals(other.value))
			return true;
		else
			return false;		
	}
	
	protected static abstract class AbstractHeaderModel implements ModelDefinition {
		public static final String NAME = "name";
		public static final String VALUE = "value";
		
		public String[] attributes() { return new String[] { NAME, VALUE }; }
		public Relationship[] relationships() { return new Relationship[] {}; }
	
	}
	public static abstract class Post {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "post";
			public String key() { return KEY; }
		}
	}
	public static abstract class Header {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "header";
			public String key() { return KEY; }
		}
	}
	public static abstract class Cookie {
		public static class Model extends AbstractHeaderModel {
			public static String KEY = "cookie";
			public String key() { return KEY; }
		}
	}
}