package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.database.Reference;

public class Variables {
	private final Hashtable variables = new Hashtable();
	/**
	 * Map a key to a value.
	 * @param key A Reference key.
	 * @param value A string value.
	 * @return The Variables object, for chaining.
	 * @throws NullPointerException if the key or value is null.
	 */
	public Variables put(Reference key, String value) throws NullPointerException {
		variables.put(key, value);
		return this;
	}
	public String get(Reference key) throws NullPointerException {
		return (String) variables.get(key);
	}
	public boolean containsKey(Reference key) {
		return variables.containsKey(key);
	}
	public String toString() {
		return variables.toString();
	}
	public Reference[] keys() {
		Reference[] keys = new Reference[size()];
		Utils.hashtableKeys(variables, keys);
		return keys;
	}
	public String[] values() {
		String[] values = new String[size()];
		Utils.hashtableValues(variables, values);
		return values;
	}
	public int size() {
		return variables.size();
	}
	public boolean equals(Variables other) {
		if(this.size() != other.size()) {
			return false; // Must be the same size.
		}
		Reference[] keys = this.keys();
		for(int i = 0; i < keys.length; i ++) {
			if(other.containsKey(keys[i])) { // Must contain the same keys.
				if(!this.get(keys[i]).equals(other.get(keys[i]))) { // The keys must be mapped to the same value.
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
