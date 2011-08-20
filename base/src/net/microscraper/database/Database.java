package net.microscraper.database;

import java.io.IOException;

/**
 * Implementations of {@link Database} store the results of {@link Execution}s
 * and generate {@link Result} objects.
 * @see Result
 * @author john
 *
 */
public interface Database {
	
	/**
	 * Store a name and value without a source {@link Result} in the {@link Database}.
	 * @param name A {@link String} name to store this value under.  Cannot be <code>null</code>.
	 * @param value A {@link String} value.  Can be <code>null</code>.
	 * @param resultNum The 0-based {@link int} index of this {@link Result} within its 
	 * {@link Execution}.
	 * redundant results -- entire pages, for example.
	 * @return A unique {@link int} identifier.
	 * @throws IOException
	 * @throws TableManipulationException.
	 */
	public int storeInitial(String name, String value, int resultNum) throws IOException, TableManipulationException;
	
	/**
	 * Store a name and value with a source {@link Result} in the {@link Database}.
	 * @param sourceId The {@link String} name of the source {@link Result}.
	 * @param sourceId The {@link int} ID of the source {@link Result}.
	 * @param name A {@link String} name to store this value under.  Cannot be <code>null</code>.
	 * @param value A {@link String} value.  Can be <code>null</code>.
	 * @param resultNum The 0-based {@link int} index of this {@link Result} within its 
	 * {@link Execution}.
	 * @return A unique {@link int} identifier.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public int store(String sourceName, int sourceId, String name, String value, int resultNum)
				throws IOException, TableManipulationException;
	

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
	
}
