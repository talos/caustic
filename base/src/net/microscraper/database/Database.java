package net.microscraper.database;

import java.io.IOException;

/**
 * Implementations of {@link Database} store the results of an {@link Executable}s and return
 * unique integer IDs.
 * @see Result
 * @author john
 *
 */
public interface Database {

	/**
	 * Store one result from an {@link Action} without a value.  This result will be shared with its
	 * source.
	 * @param sourceId The {@link int} ID of the source.
	 * @param name The {@link String} name of the blank result.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int storeOneToOne(int sourceId, String name) throws TableManipulationException, IOException;
	
	/**
	 * Store one result from an {@link Action}.  This result will be shared with its source.
	 * @param sourceId The {@link int} ID of the source.
	 * @param name A {@link String} name to store this value under. 
	 * @param value A {@link String} value.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int storeOneToOne(int sourceId, String name, String value) throws TableManipulationException, IOException;

	/**
	 * Store one result from an {@link Action}.  This result will not be shared with its source.
	 * @param sourceId The {@link int} ID of the source.
	 * @param name The {@link String} name of the blank result.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int storeOneToMany(int sourceId, String name) throws TableManipulationException, IOException;
	
	/**
	 * Store one result from an {@link Action}.  This result will not be shared with its source.
	 * @param sourceId The {@link int} ID of the source.
	 * @param name A {@link String} name to store this value under. 
	 * @param value A {@link String} value.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int storeOneToMany(int sourceId, String name, String value)
				throws TableManipulationException, IOException;
	
	/**
	 * Close up the {@link Database}. 
	 * @throws IOException If the {@link Database} experiences an exception while closing.
	 */
	public void close() throws IOException;
	
	/**
	 * Get a {@link String} value accessible to <code>id</code> in this {@link Database}.
	 * @param id The {@link int} ID that identifies what parts of {@link Database} should be searched.
	 * @param key A {@link String} key.
	 * @return A {@link String} value, or <code>null</code> if there is no value for <code>key</code>.
	 * @see #containsKey(int, String)
	 */
	public String get(int id, String key);
	
	/**
	 * Obtain an {@link int} that refers to an empty section of the {@link Database}.
	 * @throws IOException If there is a problem opening the {@link Database}.
	 */
	public int getFreshSourceId() throws IOException;
	
	/**
	 * Obtain a {@link String} representation of the names and values available for <code>id</code>.
	 * @param id
	 * @return A {@link String} representing what is available to <code>id</code>.
	 */
	public String toString(int id);
	
}
