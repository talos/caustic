package net.microscraper.database;

import net.microscraper.instruction.Result;

/**
 * Implementations of {@link Database} store the results of {@link Executable}s
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
	 * {@link Executable}.
	 * redundant results -- entire pages, for example.
	 * @return A unique {@link int} identifier.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public int store(String name, String value, int resultNum) throws DatabaseException;
	
	/**
	 * Store a name and value with a source {@link Result} in the {@link Database}.
	 * @param sourceId The {@link String} name of the source {@link Result}.
	 * @param sourceId The {@link int} ID of the source {@link Result}.
	 * @param name A {@link String} name to store this value under.  Cannot be <code>null</code>.
	 * @param value A {@link String} value.  Can be <code>null</code>.
	 * @param resultNum The 0-based {@link int} index of this {@link Result} within its 
	 * {@link Executable}.
	 * @return A unique {@link int} identifier.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public int store(String sourceName, int sourceId, String name, String value, int resultNum) throws DatabaseException;
	
	/**
	 * Close up the {@link Database}, performing whatever cleaning actions should be performed
	 * on it before the {@link IOConnection} is closed.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public void close() throws DatabaseException;
	
}
