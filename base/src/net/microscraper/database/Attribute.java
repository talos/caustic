package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;

public class Attribute {
	public static class Attributes {
		private final Hashtable attributes = new Hashtable();
		public void put(AttributeDefinition def, String value) {
			put(def, value);
		}
		public String get(AttributeDefinition def) {
			return (String) attributes.get(def);
		}
		public String get(AttributeDefinition def, Variables variables) throws TemplateException, MissingVariable {
			return (String) Mustache.compile(get(def), variables);
		}
	}
	public static class AttributeDefinition {
		public final String name;
		public AttributeDefinition(String name) {
			this.name = name;
		}
	}
}