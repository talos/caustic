package net.microscraper.util;

import java.util.Hashtable;

/**
 * A {@link Hashtable} wrapper with non-<code>null</code> {@link String} keys and values.
 * @author john
 * @see Hashtable
 *
 */
public interface Variables {
	/**
	 * 
	 * @param key A {@link String} key.
	 * @return A {@link String} value.
	 * @see Hashtable#get
	 * @see #containsKey(String key)
	 */
	public String get(String key);
	
	/**
	 * Tests if the specified object is a key in this {@link Variables}. 
	 * @param key The possible {@link String} key 
	 * @return <code>true</code> if and only if the specified <code>key</code> is a key
	 * in this {@link Variables}.
	 * @see Hashtable#containsKey
	 * @see #get(String key)
	 */
	public boolean containsKey(String key);
}
