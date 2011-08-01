package net.microscraper.interfaces.database;

import net.microscraper.Client;
import net.microscraper.executable.Executable;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.json.JSONLocation;

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
	 * Store a {@link Result}.
	 * @param result The {@link Result} to store.
	 * @return A {@link int} to identify this {@link Result}.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public int store(Result result) throws DatabaseException;
	
	/**
	 * Generate a new {@link Table}.
	 * @param tableName The {@link String} name of the {@link Table} to generate.
	 * @param textColumns An array of {@link String} column names to create as text
	 * columns.
	 * @return The new {@link Table}.
	 * @throws DatabaseException if the {@link Table} cannot be created.
	 */
	public Table newTable(String tableName, String[] textColumns) throws DatabaseException;
	
	/**
	 * Close the {@link Database}.
	 * @throws DatabaseException If there is a problem closing the {@link Database}.
	 */
	public void close() throws DatabaseException;
	
}
