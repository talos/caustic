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
	 * Store one result from an {@link Action}.
	 * @param sourceId The {@link int} ID of the source {@link Variables}.
	 * @param name A {@link String} name to store this value under.  Should not be <code>null</code>.
	 * @param value A {@link String} value.  Can be <code>null</code>.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int store(int sourceId, String name, String value) throws TableManipulationException, IOException;

	/**
	 * Store one result from an {@link Action}.
	 * @param sourceId The {@link int} ID of the source {@link Variables}.
	 * @param resultNum The 0-based {@link int} index of this result within its 
	 * {@link Executable}.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int store(int sourceId, int resultNum) throws TableManipulationException, IOException;
	
	/**
	 * Store one result from an {@link Action}.
	 * @param sourceId The {@link int} ID of the source {@link Variables}.
	 * @param resultNum The 0-based {@link int} index of this result within its 
	 * {@link Executable}.
	 * @param name A {@link String} name to store this value under.  Should not be <code>null</code>.
	 * @param value A {@link String} value.  Can be <code>null</code>.
	 * @return A unique {@link int} identifier.
	 * @throws TableManipulationException if there is a problem manipulating tables during storage.
	 * @throws IOException if there is some other problem with writing to the {@link Database}.
	 */
	public int store(int sourceId, int resultNum, String name, String value)
				throws TableManipulationException, IOException;
	
	/**
	 * Clean up the {@link Database}, preparing the tables for closing.  Does not {@link #close()}
	 * the {@link Database}.
	 * @throws TableManipulationException if there is a problem manipulating tables during cleaning.
	 */
	public void clean() throws TableManipulationException;

	/**
	 * Close up the {@link Database}.  {@link #clean()} should be performed beforehand.
	 * @throws IOException If the {@link Database} experiences an exception while closing.
	 */
	public void close() throws IOException;
	
	public int getFirstId();
}
