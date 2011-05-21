package net.microscraper.client.impl;

public interface SQLInterface {
	public static interface Cursor {
		public abstract boolean next() throws SQLInterfaceException;
		
		public abstract String getString(String columnName) throws SQLInterfaceException;
		public abstract int getInt(String columnName) throws SQLInterfaceException;
		
		public abstract void close() throws SQLInterfaceException;
	}
	
	public static interface PreparedStatement {
		public abstract Cursor query() throws SQLInterfaceException;
		public abstract boolean execute() throws SQLInterfaceException;
		public abstract Cursor executeQuery() throws SQLInterfaceException;
		
		//public abstract void setString(int index, String value) throws SQLInterfaceException;
		public abstract void bindStrings(String[] strings) throws SQLInterfaceException;
		public abstract void addBatch() throws SQLInterfaceException;
		public abstract int[] executeBatch() throws SQLInterfaceException;
	}
	/**
	 * For example, "INTEGER".
	 * @return
	 */
	public abstract String idColumnType();
	
	/**
	 * For example, "PRIMARY KEY".  Used in the table definition.
	 * @return
	 */
	public abstract String keyColumnDefinition();
	public abstract String varcharColumnType();
	public abstract String textColumnType();
	public abstract String intColumnType();
	public abstract String nullValue();

	public abstract int defaultVarcharLength();
	
	public abstract void disableAutoCommit() throws SQLInterfaceException;
	public abstract void enableAutoCommit() throws SQLInterfaceException;
	public abstract void commit() throws SQLInterfaceException;
	
	public abstract PreparedStatement prepareStatement(String sql) throws SQLInterfaceException;
	
	public static final class SQLInterfaceException extends Exception {
		private static final long serialVersionUID = 1L;
		public SQLInterfaceException(String message) { super(message); }
		public SQLInterfaceException(Throwable e) { super(e); }
		public SQLInterfaceException(String message, Throwable e) { super(message, e); }
	}

}
