package com.invisiblearchitecture.scraper;

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

	public abstract String idColumnName();
	public abstract String idColumnType();
	public abstract String dataColumnType();
	public abstract String fieldQuotation();
	public abstract String valueQuotation();
	
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
