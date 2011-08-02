package net.microscraper.interfaces.sql;

/**
 * A precompiled SQL statement.  Obtained from {@link SQLConnection#prepareStatement(String)}.
 * @author talos
 * @see SQLConnection#prepareStatement(String)
 *
 */
public interface SQLPreparedStatement {
	
	/**
	 * Execute a {@link SQLPreparedStatement} query that will return a {@link SQLResultSet}.
	 * @return The {@link SQLResultSet} from this {@link SQLPreparedStatement}'s query.
	 * @throws SQLConnectionException if there is an error executing the {@link SQLPreparedStatement},
	 * or if it does not return a {@link SQLResultSet}.
	 */
	public abstract SQLResultSet executeQuery() throws SQLConnectionException;
	
	/**
	 * Execute any sort of {@link SQLPreparedStatement}, including those that do not return
	 * a {@link SQLResultSet}.
	 * @throws SQLConnectionException if there is an error executing the {@link SQLPreparedStatement}.
	 */
	public abstract void execute() throws SQLConnectionException;
	
	//public abstract void setString(int index, String value) throws SQLInterfaceException;
	
	/**
	 * Bind an array of {@link String}s to the {@link SQLPreparedStatement}.  The first
	 * element will replace the first <code>?</code>, the second the element the second
	 * <code>?</code>, and so on.
	 * @param strings The array of {@link String}s to bind.
	 * @throws SQLConnectionException if there are more <code>strings</code> than places
	 * to bind them to, or there is an error with the {@link SQLConnection}.
	 * @see #addBatch()
	 */
	public abstract void bindStrings(String[] strings) throws SQLConnectionException;
	
	/**
	 * Add the current set of parameters to this {@link SQLPreparedStatement}'s batch 
	 * for execution.
	 * @throws SQLConnectionException
	 * @see #executeBatch()
	 */
	public abstract void addBatch() throws SQLConnectionException;
	
	/**
	 * Submits a batch of commands to the database for execution and if all commands
	 * execute successfully, returns an array of update counts.
	 * @return An {@link int} array of update counts.
	 * @throws SQLConnectionException if there is a problem with {@link SQLConnection},
	 * or if one of the statements in the batch fails.
	 */
	public abstract int[] executeBatch() throws SQLConnectionException;
}