package net.caustic.database.sql;

import java.util.List;
import java.util.Map;

import net.caustic.database.IOConnection;

/**
 * An interface to a SQL connection.
 * @author talos
 *
 */
interface SQLConnection extends IOConnection {
	
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
	 * Immediately commit all {@link SQLPreparedStatement}s that have not yet been executed in
	 * {@link SQLConnection}.
	 * @throws SQLConnectionException If there is a problem executing one of the statements.
	 */
	public abstract void commit() throws SQLConnectionException;
	
	public abstract boolean doesTableHaveColumn(String tableName, String columnName)
			throws SQLConnectionException;
	
	public abstract List<Map<String, String>> select(String sql, String[] columnNames)
			throws SQLConnectionException;
	
	public abstract List<Map<String, String>> select(String sql, String[] parameters,
			String[] columnNames) throws SQLConnectionException;
	
	public abstract void executeNow(String sql) throws SQLConnectionException;
	
	public abstract void batchModify(String sql, String[] parameters) throws SQLConnectionException;
}
