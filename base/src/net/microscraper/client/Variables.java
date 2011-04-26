package net.microscraper.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;

public class Variables {
	private final Hashtable variables;
	public Variables() {
		variables = new Hashtable();
	}
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
	/**
	 * This alters the subject Variables.  If passed 'null', nothing is changed.
	 * @param other
	 * @return
	 */
	public Variables merge(Variables other) {
		if(other != null) {
			Utils.hashtableIntoHashtable(other.variables, this.variables);
		}
		return this;
	}
	

	/**
	 * Simulate Variables from a form-style parameter string, like
	 * key1=val1&key2=val2 ...
	 * @param params_string
	 * @param encoding
	 * @return
	 */
	public static Variables fromFormParams(String params_string, String encoding) {
		Variables variables = new Variables();
		String[] params = Utils.split(params_string, "&");
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				String name = URLDecoder.decode(name_value[0], encoding);
				String value = URLDecoder.decode(name_value[1], encoding);
				variables.put(name, value);
			}
			return variables;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding " + encoding + " not supported: " + e.getMessage());
		}
	}
}
