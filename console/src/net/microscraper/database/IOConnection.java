package net.microscraper.database;

/**
 * A {@link Connection} that can obtain {@link IOTable}s.
 * @author talos
 * @see #newIOTable(String, String[])
 *
 */
public interface IOConnection extends Connection {

	/**
	 * 
	 * @return The {@link String} name of the scope column for all tables in 
	 * {@link SQLConnection}.
	 */
	public abstract String getScopeColumnName();
	
	/**
	 * Obtain a new {@link IOTable} using this {@link IOConnection}.  If the table
	 * may exist already, {@link #getIOTable(String)} should be called to check
	 * and obtain the {@link IOTable} instead.
	 * @param name The {@link String} name of the new {@link IOTable}.
	 * @param columnNames An array of {@link String} columns to include in this 
	 * {@link IOTable}.
	 * @return A {@link IOTable}.
	 * @throws ConnectionException if the {@link IOTable} cannot be created.
	 */
	public abstract IOTable newIOTable(String name, String[] columnNames)
			throws ConnectionException;

	/**
	 * Obtain an existing {@link IOTable} using this {@link IOConnection}.
	 * @param name The {@link String} name of the new {@link IOTable}.
	 * @return A {@link IOTable} if the table exists, <code>null</code> otherwise.
	 * @throws ConnectionException if the {@link IOTable} cannot be created.
	 */
	public abstract IOTable getIOTable(String name) throws ConnectionException;
}