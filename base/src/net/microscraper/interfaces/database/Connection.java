package net.microscraper.interfaces.database;

public interface Connection {

	/**
	 * Open the {@link Connection}.
	 * @throws DatabaseException If there is a problem opening the {@link Connection}.
	 */
	public abstract void open() throws DatabaseException;

	/**
	 * Obtain a new {@link Table} using this {@link Connection}.
	 * @param name The {@link String} name of the new {@link Table}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link Table}.
	 * @return A {@link Table}.
	 * @throws DatabaseException if the {@link Table} cannot be created.
	 */
	public abstract Table getTable(String name, String[] textColumns)
			throws DatabaseException;

	/**
	 * Close the {@link Connection}.
	 * @throws DatabaseException If there is a problem closing the {@link Connection}.
	 */
	public abstract void close() throws DatabaseException;

}