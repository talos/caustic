package net.caustic.database;


/**
 * A basic interface for connections.
 * @author talos
 * @see #open()
 * @see #close()
 *
 */
public interface Connection {

	/**
	 * Open the {@link Connection}.
	 * @throws ConnectionException If there is a problem closing the {@link Connection}.
	 */
	public abstract void open() throws ConnectionException;
	

	/**
	 * 
	 * @return The {@link String} name of the scope column for all tables in 
	 * {@link SQLConnection}.
	 */
	public abstract String getScopeColumnName();
	
	/**
	 * Obtain a new {@link Table} using this {@link IOConnection}.  If the table
	 * may exist already, {@link #getTable(String)} should be called to check
	 * and obtain the {@link Table} instead.
	 * @param name The {@link String} name of the new {@link Table}.
	 * @param columnNames An array of {@link String} columns to include in this 
	 * {@link Table}.
	 * @param keyColumnNames An array of {@link String} columns to use as the primary
	 * key of this {@link Table}.  Provide an empty array for no primary key, a single
	 * column for a regular primary key, and multiple columns for a composite primary key.
	 * @return A {@link Table}.
	 * @throws ConnectionException if the {@link Table} cannot be created.
	 */
	public abstract Table newTable(String name, String[] columnNames,
				String[] keyColumnNames)
			throws ConnectionException;

	/**
	 * Obtain an existing {@link Table} using this {@link IOConnection}.
	 * @param name The {@link String} name of the new {@link Table}.
	 * @return A {@link Table} if the table exists, <code>null</code> otherwise.
	 * @throws ConnectionException if the {@link Table} cannot be created.
	 */
	public abstract Table getTable(String name) throws ConnectionException;

	/**
	 * Close the {@link Connection}.
	 * @throws ConnectionException If there is a problem closing the {@link Connection}.
	 */
	public abstract void close() throws ConnectionException;

}