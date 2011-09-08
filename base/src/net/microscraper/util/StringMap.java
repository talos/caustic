package net.microscraper.util;

import java.util.Hashtable;

public interface StringMap {

	/**
	 * 
	 * @return A new {@link StringMap} that will check
	 * this {@link StringMap} only if it cannot find a key
	 * in itself.
	 * @param name A {@link String} name for the child.
	 */
	public abstract StringMap spawnChild(String name);

	/**
	 * 
	 * @return A new {@link StringMap} that will check
	 * this {@link StringMap} only if it cannot find a key
	 * in itself.
	 * @param name A {@link String} name for the child, that
	 * will also be the key for <code>value</code>.
	 * @param value A {@link String} value to attach to
	 * <code>name</code>.
	 */
	public abstract StringMap spawnChild(String name, String value);

	/**
	 * Get a {@link String} value mapped to a key.
	 * @param key The {@link String} key to look for.
	 * @return The {@link String} value if it is contained
	 * in this {@link StringMap} or one of its parents,
	 * <code>null</code> otherwise.
	 */
	public abstract String get(String key);

	/**
	 * Map a {@link String} value to a {@link String} key.
	 * Modifies the backing {@link Hashtable}, but does not
	 * touch any parent {@link StringMap}s.  <code>null</code>
	 * is not allowed for the key or value.
	 * @param key the {@link String} key.
	 * @param value the {@link String} value.
	 */
	public abstract void put(String key, String value);

}