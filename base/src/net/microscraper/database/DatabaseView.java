package net.microscraper.database;

import java.io.IOException;

import net.microscraper.template.StringTemplate;

/**
 * Microscraper uses {@link DatabaseView} to store data from {@link Scraper}s
 * for future use when substituting in {@link StringTemplate}.
 * @author talos
 *
 */
public interface DatabaseView {

	/**
	 * 
	 * @param name A {@link String} name for the child.
	 * @return A new {@link DatabaseView} that will check
	 * this {@link DatabaseView} only if it cannot find a key
	 * in itself.
	 * @throws IOException if there was a problem persisting.
	 */
	public abstract DatabaseView spawnChild(String name) throws IOException;

	/**
	 * 
	 * @param name A {@link String} name for the child, that
	 * will also be the key for <code>value</code>.
	 * @param value A {@link String} value to attach to
	 * <code>name</code> within the child only.
	 * @return A new {@link DatabaseView} that will check
	 * this {@link DatabaseView} only if it cannot find a key
	 * in itself.
	 * @throws IOException if there was a problem persisting.
	 */
	public abstract DatabaseView spawnChild(String name, String value) throws IOException;

	/**
	 * Get a {@link String} value mapped to a key.
	 * @param key The {@link String} key to look for.
	 * @return The {@link String} value if it is contained
	 * in this {@link DatabaseView} or one of its parents,
	 * <code>null</code> otherwise.
	 */
	public abstract String get(String key);

	/**
	 * Map a {@link String} value to a {@link String} key.
	 * Modifies this {@link DatabaseView}, but does not
	 * touch any parent {@link DatabaseView}s.  <code>null</code>
	 * is not allowed for the key or value.
	 * @param key the {@link String} key.
	 * @param value the {@link String} value.
	 * @throws IOException if there was a problem persisting.
	 */
	public abstract void put(String key, String value) throws IOException;

}