package net.microscraper.database;

import java.io.IOException;

/**
 * Implementations of {@link Database} store the results of an {@link Executable}s and return
 * unique {@link Scope}.
 * @see ScraperResult
 * @author john
 *
 */
public interface DatabaseDeprecated {

	/**
	 * Store one result from an {@link Action} without a value.  This result will be shared with its
	 * source.
	 * @param source The {@link Scope} of the source.
	 * @param name The {@link String} name of the blank result.
	 * @throws IOException if there is a problem writing to the {@link Database}.
	 */
	public void storeOneToOne(Scope source, String name) throws IOException;
	
	/**
	 * Store one result from an {@link Action}.  This result will be shared with its source.
	 * @param source The {@link Scope} of the source.
	 * @param name A {@link String} name to store this value under. 
	 * @param value A {@link String} value.
	 * @throws IOException if there is a problem writing to the {@link Database}.
	 */
	public void storeOneToOne(Scope source, String name, String value) throws IOException;
	
	/**
	 * Store one result from an {@link Action}.  This result will not be shared with its source,
	 * resulting in a new {@link Scope}.
	 * @param source The {@link Scope} of the source.
	 * @param name The {@link String} name of the blank result.
	 * @return The new {@link Scope}.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public Scope storeOneToMany(Scope source, String name) throws IOException;
	
	/**
	 * Store one result from an {@link Action}.  This result will not be shared with its source,
	 * resulting in a new {@link Scope}.
	 * @param source The {@link Scope} of the source.
	 * @param name A {@link String} name to store this value under. 
	 * @param value A {@link String} value.
	 * @return The new {@link Scope}.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public Scope storeOneToMany(Scope source, String name, String value)
				throws IOException;
	
	/**
	 * Get a {@link String} value accessible to <code>id</code> in this {@link Database}.
	 * @param scope The {@link Scope} that identifies what parts of {@link Database} should be searched.
	 * @param key A {@link String} key.
	 * @return A {@link String} value, or <code>null</code> if there is no value for <code>key</code>.
	 */
	public String get(Scope scope, String key);
	
	/**
	 * @return A default {@link Scope} that refers to an empty section of the {@link Database}.
	 * @throws IOException If there is a problem with the {@link Database}.
	 * @see {@link Scope#getDefault(net.microscraper.util.UUID)}
	 */
	public Scope getDefaultScope() throws IOException;
	
	/**
	 * @param scope The {@link Scope} to use.
	 * @return A {@link String} representing the data available to <code>scope</code>.
	 */
	public String toString(Scope scope);

	/**
	 * Open and initialize the database.  This will also open any underlying connections.
	 * @throws IOException If there was a problem opening the {@link Database}.
	 */
	public void open() throws IOException;
	
	/**
	 * Close the database.  This will also close any underlying connections.
	 * @throws IOException If there was a problem closing the {@link Database}.
	 */
	public void close() throws IOException;
}
