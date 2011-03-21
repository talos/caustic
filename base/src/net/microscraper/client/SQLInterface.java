package net.microscraper.client;

public interface SQLInterface {
	public static interface CursorInterface {
		public abstract boolean next() throws SQLInterfaceException;
		
		public abstract String getString(String columnName) throws SQLInterfaceException;
		public abstract int getInt(String columnName) throws SQLInterfaceException;
		
		public abstract void close() throws SQLInterfaceException;
	}
	
	/*
	public static interface Statement {
		public abstract void bindString(int index, String value) throws Exception;
		
		public abstract Cursor execute() throws Exception;
	}
	*/
	
	public abstract CursorInterface query(String sql) throws SQLInterfaceException;
	public abstract boolean execute(String sql) throws SQLInterfaceException;
	
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
	public abstract String dataColumnType();
	/**
	 * Quote a field.
	 * @param field
	 * @return
	 * @throws SQLInterfaceException If the supplied field is null.
	 */
	public abstract String quoteField(String field) throws SQLInterfaceException;
	public abstract String quoteValue(String value);
	public abstract String nullValue();
	
	public static final class SQLInterfaceException extends Throwable {
		private static final long serialVersionUID = 1L;
		public SQLInterfaceException(String message) {
			super(message);
		}
		public SQLInterfaceException(Throwable e) {
			super(e);
		}
	}

}
