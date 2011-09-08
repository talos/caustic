package net.microscraper.util;

import java.util.Hashtable;

import net.microscraper.template.StringTemplate;

/**
 * Microscraper uses {@link StringMap} to store data from {@link Scraper}s
 * for future use when substituting in {@link StringTemplate}.
 * @author talos
 *
 */
public class StringMap {
	private final Hashtable hashtable;
	private final StringMap parent;

	/**
	 * Construct a new {@link StringMap} without any values.
	 */
	public StringMap() {
		this.hashtable = new Hashtable();
		this.parent = null;
	}
	
	/**
	 * Construct a new {@link StringMap} backed by
	 * <code>hashtable</code>, which will be modified.
	 * @param hashtable A backing {@link Hashtable}.
	 */
	public StringMap(Hashtable hashtable) {
		this.hashtable = hashtable;
		this.parent = null;
	}
	
	/**
	 * Private constructor, used by {@link #spawnChild()}.
	 * @param parent The parent {@link StringMap} to check
	 * if the backing {@link Hashtable} has no entry.
	 */
	private StringMap(StringMap parent) {
		this.parent = parent;
		this.hashtable = new Hashtable();
	}
	
	/**
	 * 
	 * @return A new {@link StringMap} that will check
	 * this {@link StringMap} only if it cannot find a key
	 * in itself.
	 */
	public StringMap spawnChild() {
		return new StringMap(this);
	}
	
	/**
	 * Get a {@link String} value mapped to a key.
	 * @param key The {@link String} key to look for.
	 * @return The {@link String} value if it is contained
	 * in this {@link StringMap} or one of its parents,
	 * <code>null</code> otherwise.
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
	 * Map a {@link String} value to a {@link String} key.
	 * Modifies the backing {@link Hashtable}, but does not
	 * touch any parent {@link StringMap}s.  <code>null</code>
	 * is not allowed for the key or value.
	 * @param key the {@link String} key.
	 * @param value the {@link String} value.
	 */
	public void put(String key, String value) {
		hashtable.put(key, value);
	}
}
