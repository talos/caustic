package com.invisiblearchitecture.scraper;

public interface SQLiteInterface {
	public static interface CursorInterface {
		public abstract boolean next() throws Exception;
		
		public abstract String getString(String columnName) throws Exception;
		public abstract int getInt(String columnName) throws Exception;
		
		public abstract void close() throws Exception;
	}
	
	/*
	public static interface Statement {
		public abstract void bindString(int index, String value) throws Exception;
		
		public abstract Cursor execute() throws Exception;
	}
	*/
	
	public abstract CursorInterface query(String sql) throws Exception;
}
