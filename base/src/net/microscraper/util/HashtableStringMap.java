package net.microscraper.util;

import java.util.Hashtable;

import net.microscraper.template.StringTemplate;

/**
 * Microscraper uses {@link StringMap} to store data from {@link Scraper}s
 * for future use when substituting in {@link StringTemplate}.
 * @author talos
 *
 */
public class HashtableStringMap implements StringMap {
	private final Hashtable hashtable;
	private final StringMap parent;

	/**
	 * Construct a new {@link StringMap} without any values.
	 */
	public HashtableStringMap() {
		this.hashtable = new Hashtable();
		this.parent = null;
	}
	
	/**
	 * Construct a new {@link StringMap} backed by
	 * <code>hashtable</code>, which will be modified.
	 * @param hashtable A backing {@link Hashtable}.
	 */
	public HashtableStringMap(Hashtable hashtable) {
		this.hashtable = hashtable;
		this.parent = null;
	}
	
	/**
	 * Private constructor, used by {@link #spawnChild()}.
	 * @param parent The parent {@link StringMap} to check
	 * if the backing {@link Hashtable} has no entry.
	 */
	private HashtableStringMap(StringMap parent) {
		this.parent = parent;
		this.hashtable = new Hashtable();
	}
	
	public StringMap spawnChild(String name) {
		return new HashtableStringMap(this);
	}

	
	public StringMap spawnChild(String name, String value) {
		StringMap child = new HashtableStringMap(this);
		child.put(name, value);
		return child;
	}
	
	public String get(String key) {
		if(hashtable.containsKey(key)) {
			return (String) hashtable.get(key);
		} else if(parent != null) {
			return parent.get(key);
		} else {
			return null;
		}
	}
	
	public void put(String key, String value) {
		hashtable.put(key, value);
	}
}
