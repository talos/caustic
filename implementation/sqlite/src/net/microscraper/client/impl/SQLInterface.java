package net.microscraper.client.impl;

public interface SQLInterface {
	public static interface Cursor {
		public abstract boolean next() throws SQLInterfaceException;
		
		public abstract String getString(String columnName) throws SQLInterfaceException;
		public abstract int getInt(String columnName) throws SQLInterfaceException;
		
		public abstract void close() throws SQLInterfaceException;
	}
	
	public static interface Statement {
		public abstract void bindString(int index, String value) throws SQLInterfaceException;
		
		public abstract boolean execute() throws SQLInterfaceException;
		public abstract Cursor executeQuery() throws SQLInterfaceException;
	}
	
	public abstract Cursor query(String sql) throws SQLInterfaceException;
	public abstract boolean execute(String sql) throws SQLInterfaceException;
	public abstract Cursor query(String sql, String[] substitutions) throws SQLInterfaceException;
	public abstract boolean execute(String sql, String[] substitutions) throws SQLInterfaceException;
	
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
	
	public static final class SQLInterfaceException extends Throwable {
		private static final long serialVersionUID = 1L;
		public SQLInterfaceException(String message) { super(message); }
		public SQLInterfaceException(Throwable e) { super(e); }
		public SQLInterfaceException(String message, Throwable e) { super(message, e); }
	}

}
