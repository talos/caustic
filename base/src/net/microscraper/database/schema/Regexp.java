package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Regexp {
	public final Interfaces.Regexp.Pattern pattern;
	public Regexp(Resource resource, Interfaces.Regexp regex_interface, Hashtable variables)
					throws TemplateException, MissingVariable {
		pattern = regex_interface.compile(Mustache.compile(resource.attribute_get(Model.REGEXP), variables));
	}
	public Regexp(String pattern_string, Interfaces.Regexp regex_interface, Hashtable variables)
					throws TemplateException, MissingVariable {
		pattern = regex_interface.compile(Mustache.compile(pattern_string, variables));
	}
	
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