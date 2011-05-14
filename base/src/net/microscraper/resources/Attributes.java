package net.microscraper.resources;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Utils.HashtableWithNulls;

/**
 * Store and retrieve a resource's attributes.
 * @author john
 *
 */
public class Attributes {
	private final HashtableWithNulls attributes = new HashtableWithNulls();
	public Attributes(AttributeDefinition[] definitions, Interfaces.JSON.Object jsonObject) throws JSONInterfaceException {
		for(int i = 0 ; i < definitions.length ; i ++) {
			Object rawValue = jsonObject.get(definitions[i].name);
			if(rawValue.equals(null)) {
				rawValue = null;
			}
			attributes.put(definitions[i].name, rawValue);
		}
	}
	public String getString(AttributeDefinition def) {
		return (String) attributes.get(def.name);
	}
	public Integer getInteger(AttributeDefinition def) {
		return (Integer) attributes.get(def.name);
	}
	public boolean getBoolean(AttributeDefinition def) {
		return ((Boolean) attributes.get(def.name)).equals(Boolean.TRUE);
	}
}
