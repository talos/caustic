package net.microscraper.database;

import java.util.Hashtable;

public class Attribute {
	public static class Attributes {
		private final Hashtable attributes = new Hashtable();
		public void put(AttributeDefinition def, String value) {
			attributes.put(def, value);
		}
		public String get(AttributeDefinition def) {
			return (String) attributes.get(def);
		}
	}
	public static class AttributeDefinition {
		public final String name;
		public AttributeDefinition(String name) {
			this.name = name;
		}
	}
}