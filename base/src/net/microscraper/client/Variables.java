package net.microscraper.client;

import java.util.Hashtable;

/**
 * A &lt;String, String&gt; Hashtable for JME.
 * @author realest
 *
 */
public final class Variables {
	private final Hashtable variables;
	public Variables() {
		variables = new Hashtable();
	}
	/**
	 * @see Hashtable#put
	 * @param key A string key.
	 * @param value A string value.
	 * @throws NullPointerException if the key or value is null
	 */
	public void put(String key, String value) {
		variables.put(key, value);
	}
	/**
	 * @see Hashtable#containsKey
	 * @param key A string key.
	 * @return <code>true</code> if and only if the specified object is a key in this hashtable, as determined by the equals method; false otherwise. 
	 * @throws NullPointerException if the key is null
	 */
	public boolean containsKey(String key) {
		return variables.containsKey(key);
	}
	/**
	 * @see Hashtable#get
	 * @param key A string key to retrieve.
	 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key 
	 * @throws NullPointerException if the specified key is null
	 */
	public String get(String key) {
		return (String) variables.get(key);
	}
	/**
	 * @see Hashtable#toString
	 */
	public String toString() {
		return variables.toString();
	}
	/*
	public String[] keys() {
		String[] keys = new String[variables.size()];
		Enumeration e = variables.keys();
		for(int i = 0 ; e.hasMoreElements() ; i ++) {
			keys[i] = (String) e.nextElement();
		}
		return keys;
	}*/
	public Variables extend(Variables other) {
		if(other != null) {
			Utils.hashtableIntoHashtable(other.variables, this.variables);
		}
		return this;
	}
	/*
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
	}*/
}