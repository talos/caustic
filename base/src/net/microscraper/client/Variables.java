package net.microscraper.client;

import java.util.Enumeration;
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
	public String[] keys() {
		String[] keys = new String[variables.size()];
		Enumeration e = variables.keys();
		for(int i = 0 ; e.hasMoreElements() ; i ++) {
			keys[i] = (String) e.nextElement();
		}
		return keys;
	}
	public Variables merge(Variables variables) {
		variables = new Variables();
	}
}
