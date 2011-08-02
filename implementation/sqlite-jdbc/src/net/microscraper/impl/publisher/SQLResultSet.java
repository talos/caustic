package net.microscraper.impl.publisher;


/**
 * An interface to parse the results of a SQL query. Obtained from a {@link SQLPreparedStatement}.
 * @author talos
 * @see SQLPreparedStatement#query()
 * @see SQLPreparedStatement#executeQuery()
 *
 */
public interface SQLResultSet {
	
	/**
	 * Advance to the next row of the {@link SQLResultSet}.  {@link SQLResultSet}
	 * starts before the first row; the first call of {@link #next()} advances to the first row,
	 * the second to the second row, and so on.
	 * @return <code>true</code> if there is another row
	 * false</code> otherwise.
	 * @throws SQLConnectionException if called on a closed {@link SQLResultSet}, or if
	 * there is some other problem with the database.
	 */
	public abstract boolean next() throws SQLConnectionException;
	
	/**
	 * Get the {@link String} value of the supplied <code>columnName</code> for the
	 * current row.
	 * @param columnName The {@link String} name of the column whose value should be obtained.
	 * @return The value of the column for this row as a {@link String}.
	 * <code>Null</code> if the value is <code>null</code>.
	 * @throws SQLConnectionException if called on a closed {@link SQLResultSet}, if
	 * <code>columnName</code> is not a column, or if there is some other problem with the 
	 * database.
	 */
	public abstract String getString(String columnName) throws SQLConnectionException;
	
	/**
	 * Get the {@link int} value of the supplied <code>columnName</code> for the
	 * current row.
	 * @param columnName The {@link int} name of the column whose value should be obtained.
	 * @return The value of the column for this row as an {@link int}. <code>Null</code>
	 * values are returned as <code>0</code>.
	 * @throws SQLConnectionException if called on a closed {@link SQLResultSet}, if
	 * <code>columnName</code> is not a column, or if there is some other problem with the 
	 * database.
	 */
	public abstract int getInt(String columnName) throws SQLConnectionException;
	
	/**
	 * Close the {@link SQLResultSet}.
	 * @throws SQLConnectionException If there is a problem with the database.
	 */
	public abstract void close() throws SQLConnectionException;
}