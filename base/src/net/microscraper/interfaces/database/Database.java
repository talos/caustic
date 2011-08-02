package net.microscraper.interfaces.database;

import net.microscraper.executable.Result;

/**
 * Implementations of {@link Database} receive and store {@link Result}s.
 * @see Result
 * @author john
 *
 */
public interface Database {
	
	/**
	 * Open the {@link Database}.
	 * @throws DatabaseException If there is a problem opening the {@link Database}.
	 */
	public void open() throws DatabaseException;
	
	/**
	 * Obtain a new {@link Table} within this {@link Database}.
	 * @param name The {@link String} name of the new {@link Table}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link Table}.
	 * @return A {@link Table}.
	 * @throws DatabaseException if the {@link Table} cannot be created.
	 */
	public Table getTable(String name, String[] textColumns) throws DatabaseException;
	
	/**
	 * Store a name and value without a source {@link Result} in the {@link Database}.
	 * @param name A {@link String} name to store this value under.
	 * @param value A {@link String} value.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(String name, String value) throws DatabaseException;
	
	/**
	 * Store a name and value with a source {@link Result} in the {@link Database}.
	 * @param source The {@link Result} source for the stored <code>name</code> and <code>
	 * value</code>.
	 * @param name A {@link String} name to store this value under.
	 * @param value A {@link String} value.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(Result source, String name, String value) throws DatabaseException;
	
	/**
	 * Close the {@link Database}.
	 * @throws DatabaseException If there is a problem closing the {@link Database}.
	 */
	public void close() throws DatabaseException;
	
}
