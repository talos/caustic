package net.microscraper.database.schema;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Regexp {
	public final Interfaces.Regexp.Pattern pattern;
	public Regexp(Resource resource, Variables variables)
					throws TemplateException, MissingVariable {
		pattern = Client.context().regexp.compile(Mustache.compile(resource.attribute_get(Model.REGEXP), variables));
	}
	public Regexp(String pattern_string, Variables variables)
					throws TemplateException, MissingVariable {
		pattern = Client.context().regexp.compile(Mustache.compile(pattern_string, variables));
	}
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Regexp))
			return false;
		Regexp other = (Regexp) obj;
		return this.pattern.toString().equals(other.pattern.toString());	
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "regexp";
	
		public static final String REGEXP = "regexp";
		
		protected String _key() { return KEY; }
		protected String[] _attributes() {
			return new String[] { REGEXP };
		}
		protected Relationship[] _relationships() {
			return new Relationship[] { };
		}
	}
}