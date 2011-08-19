package net.microscraper.util;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Variables {
	
	private final Vector parentHashtables = new Vector();
	private Hashtable hashtable = new Hashtable();
	
	private Variables() { }
	
	/**
	 * Extend a {@link BasicVariables} with mappings from a {@link Hashtable}.  Modifies
	 * the original.
	 * @param withHashtable The {@link Hashtable} to use.
	 * @throws IllegalArgumentException if one of the <code>withHashtable</code> elements
	 * has a non-{@link String} key or value.
	 * @return The original {@link BasicVariables}, modified.
	 */
	public Variables extend(Hashtable withHashtable) {
		Enumeration enum = withHashtable.keys();
		while(enum.hasMoreElements()) {
			Object key = enum.nextElement();
			Object value = withHashtable.get(key);
			if(key instanceof String && value instanceof String) {
				hashtable.put(key, value);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return this;
	}

	/**
	 * Initialize an empty {@link BasicVariables}.
	 */
	public static Variables empty() {
		return new Variables();
	}
	
	/**
	 * Turn a form-encoded {@link String} into {@link Variables}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.
	 * @param encoding The encoding to use.  <code>UTF-8</code> recommended.
	 * @return A {@link Variables}.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 */
	public static Variables fromFormEncoded(Decoder decoder, String formEncodedData, String encoding)
			throws UnsupportedEncodingException {
		String[] splitByAmpersands = StringUtils.split(formEncodedData, "&");
		Hashtable hashtable = new Hashtable();
		for(int i = 0 ; i < splitByAmpersands.length; i++) {
			String[] pair = StringUtils.split(splitByAmpersands[i], "=");
			if(pair.length == 2) {
				hashtable.put(decoder.decode(pair[0], encoding),
						decoder.decode(pair[1], encoding));
			} else {
				throw new IllegalArgumentException(StringUtils.quote(splitByAmpersands[i]) + " is not a valid name-value pair.");
			}
		}
		return fromHashtable(hashtable);
	}
	
	/**
	 * Initialize {@link BasicVariables} values from a {@link Hashtable}.
	 * @param initialHashtable Initial {@link Hashtable} whose mappings should stock {@link Variables}.
	 * @throws IllegalArgumentException if one of the <code>initialHashtable</code> elements
	 * has a non-{@link String} key or value.
	 */
	public static Variables fromHashtable(Hashtable initialHashtable) {
		return empty().extend(initialHashtable);
	}

	/**
	 * 
	 * @param key A {@link String} key.
	 * @return A {@link String} value.
	 * @see Hashtable#get
	 * @see #containsKey(String key)
	 */
	public String get(String key) {
		if(hashtable.containsKey(key)) {
			return (String) hashtable.get(key);			
		} else {
			Enumeration enumeration = parentHashtables.elements();
			while(enumeration.hasMoreElements()) {
				Hashtable parent = (Hashtable) enumeration.nextElement();
				if(parent.containsKey(key)) {
					return (String) parent.get(key);
				}
			}
		}
		return null;
	}

	/**
	 * Tests if the specified object is a key in this {@link Variables}. 
	 * @param key The possible {@link String} key 
	 * @return <code>true</code> if and only if the specified <code>key</code> is a key
	 * in this {@link Variables}.
	 * @see Hashtable#containsKey
	 * @see #get(String key)
	 */
	public boolean containsKey(String key) {
		if(hashtable.containsKey(key)) {
			return true;
		} else {
			Enumeration enumeration = parentHashtables.elements();
			while(enumeration.hasMoreElements()) {
				Hashtable parent = (Hashtable) enumeration.nextElement();
				if(parent.containsKey(key)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void put(String key, String value) {
		hashtable.put(key, value);
	}
	
	public void branch(String key, String value) {
		parentHashtables.add(hashtable);
		hashtable = new Hashtable();
		put(key, value);
	}
}
