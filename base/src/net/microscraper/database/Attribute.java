package net.microscraper.database;

import net.microscraper.client.Utils.HashtableWithNulls;

public class Attribute {
	public static class Attributes {
		private final HashtableWithNulls attributes = new HashtableWithNulls();
		public void put(AttributeDefinition def, Object value) {
			if(value != null) {
				attributes.put(def, value);
			}
		}
		public String getString(AttributeDefinition def) {
			return (String) attributes.get(def);
		}
		public Integer getInteger(AttributeDefinition def) {
			return (Integer) attributes.get(def);
		}
		public boolean getBoolean(AttributeDefinition def) {
			return ((Boolean) attributes.get(def)).equals(Boolean.TRUE);
		}
	}
	public static class AttributeDefinition {
		public final String name;
		public AttributeDefinition(String name) {
			this.name = name;
		}
	}
}