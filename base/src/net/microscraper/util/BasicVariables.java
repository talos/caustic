package net.microscraper.util;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * An implementation of {@link Variables} optionally initialized with
 * several {@link NameValuePair}s.
 * @author realest
 *
 */
public final class BasicVariables implements Variables {
	
	private final Hashtable hashtable = new Hashtable();
	
	private BasicVariables() { }
	
	/**
	 * Extend a {@link BasicVariables} with mappings from a {@link Hashtable}.  Modifies
	 * the original.
	 * @param withHashtable The {@link Hashtable} to use.
	 * @throws IllegalArgumentException if one of the <code>withHashtable</code> elements
	 * has a non-{@link String} key or value.
	 * @return The original {@link BasicVariables}, modified.
	 */
	public BasicVariables extend(Hashtable withHashtable) {
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
	public static BasicVariables empty() {
		return new BasicVariables();
	}
	
	/**
	 * Turn a form-encoded {@link String} into {@link Variables}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.
	 * @param encoding The encoding to use.  <code>UTF-8</code> recommended.
	 * @return A {@link Variables}.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 */
	public static BasicVariables fromFormEncoded(Decoder decoder, String formEncodedData, String encoding)
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
	public static BasicVariables fromHashtable(Hashtable initialHashtable) {
		return empty().extend(initialHashtable);
	}
	
	public String get(String key) {
		return (String) hashtable.get(key);
	}

	public boolean containsKey(String key) {
		return hashtable.containsKey(key);
	}
}
