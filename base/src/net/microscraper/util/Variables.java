package net.microscraper.util;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

public class Variables {
	
	private final Variables parent;
	private Hashtable hashtable = new Hashtable();
	
	private Variables() {
		parent = null;
	}
	
	private Variables(Variables parent) {
		this.parent = parent;
	}
	
	public static Variables branch(Variables parent, String key, String value) {
		Variables branched = new Variables(parent);
		branched.put(key, value);
		return branched;
	}

	/**
	 * Initialize an empty {@link Variables}.
	 */
	public static Variables empty() {
		return new Variables();
	}
	
	/**
	 * Turn a form-encoded {@link String} into {@link Variables}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.  It must be 
	 * correctly formatted.
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
	 * Initialize {@link Variables} values from a {@link Hashtable}.  Its keys and values must
	 * all be {@link String}s.
	 * @param initialHashtable Initial {@link Hashtable} whose mappings should stock {@link Variables}.
	 */
	public static Variables fromHashtable(Hashtable initialHashtable) {
		Enumeration enum = initialHashtable.keys();
		Variables variables = new Variables();
		while(enum.hasMoreElements()) {
			try {
				String key = (String) enum.nextElement();
				String value = (String) initialHashtable.get(key);
				variables.put((String) key, value);
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Variables must be initialized with String-String hashtable.", e);
			}
		}
		return variables;
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
		} else if(parent != null) {
			return parent.get(key);
		} else {
			return null;
		}
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
		} else if(parent != null) {
			return parent.containsKey(key);
		} else {
			return false;
		}
	}
	
	public void put(String key, String value) {
		hashtable.put(key, value);
	}
}
