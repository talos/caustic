package net.microscraper.client;

import java.util.Hashtable;

public class Variables {
	private final Hashtable variables = new Hashtable();
	public Variables put(String key, String value) throws NullPointerException {
		variables.put(key, value);
		return this;
	}
	public boolean containsKey(String key) {
		return variables.containsKey(key);
	}
	public String get(String key) {
		return (String) variables.get(key);
	}
	public String toString() {
		return variables.toString();
	}
}
