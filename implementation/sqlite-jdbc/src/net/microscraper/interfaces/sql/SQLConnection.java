package net.microscraper.interfaces.sql;

/**
 * An interface to a SQL connection.  Methods similar to {@link java.sql}.
 * @author talos
 *
 */
public interface SQLConnection {
	
	/**
	 * @return A {@link String} to define a primary key, for example <code>
	 * PRIMARY KEY</code>. Used when creating new tables.
	 */
	public abstract String keyColumnDefinition();
	
	/**
	 * 
	 * @return A {@link String} to define a <code>VARCHAR</code> column.  Used
	 * when creating new tables.
	 */
	public abstract String varcharColumnType();
	
	/**
	 * 
	 * @return A {@link String} to define a <code>TEXT</code> column.  Used
	 * when creating new tables.
	 */
	public abstract String textColumnType();
	
	/**
	 * @return A {@link String} to define a <code>INT</code> column.  Used
	 * when creating new tables.
	 */
	public abstract String intColumnType();
	
	/**
	 * 
	 * @return The name of <code>null</code> values in this implementation, for
	 * example <code>NULL</code>.  Used when inserting values.
	 */
	public abstract String nullValue();
	
	/**
	 * 
	 * @return How long a default <code>VARCHAR</code> column should be.
	 */
	public abstract int defaultVarcharLength();
	
	/**
	 * Disables auto-commit mode.  If auto-commit mode is on, each SQL statement
	 * in this {@link SQLConnection} will be executed as a separate transaction.
	 * If it is off, they will be executed as a batch when {@link #commit()} is 
	 * called.
	 * @throws SQLConnectionException if a database access error occurs.
	 * @see #enableAutoCommit()
	 * @see #commit()
	 */
	public abstract void disableAutoCommit() throws SQLConnectionException;
	
	/**
	 * Enables auto-commit mode.  If auto-commit mode is on, each SQL statement
	 * in this {@link SQLConnection} will be executed as a separate transaction.
	 * If it is off, they will be executed as a batch when {@link #commit()} is 
	 * called.
	 * @throws SQLConnectionException if a database access error occurs.
	 * @see #disableAutoCommit()
	 */
	public abstract void enableAutoCommit() throws SQLConnectionException;
	
	/**
	 * If auto-commit mode is off, this will execute all pending statements for this
	 * {@link SQLConnection} as a batch.
	 * @throws SQLConnectionException if there is a problem committing the statements to
	 * this {@link SQLConnection}, including if the {@link SQLConnection} is in 
	 * auto-commit mode.
	 * @see #disableAutoCommit()
	 */
	public abstract void commit() throws SQLConnectionException;
	
	/**
	 * 
	 * Generate a {@link SQLPreparedStatement} to execute upon this {@link SQLConnection}.
	 * @param sql The SQL statement as a {@link String}.
	 * @return the {@link SQLPreparedStatement}.
	 * @throws SQLConnectionException if there is a problem generating the {@link SQLPreparedStatement}.
	 */
	public abstract SQLPreparedStatement prepareStatement(String sql) throws SQLConnectionException;
	
	/**
	 * Check whether a table exists.
	 * @param tableName The {@link String} name of the table to check.
	 * @return <code>True</code> if the table exists, <code>false</code> otherwise.
	 * @throws SQLConnectionException if there is a problem with the {@link SQLConnection}.
	 */
	public boolean tableExists(String tableName) throws SQLConnectionException;

}
