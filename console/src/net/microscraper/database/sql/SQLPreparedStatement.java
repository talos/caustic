package net.microscraper.database.sql;

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
	
	public abstract void close() throws SQLConnectionException;
}