package net.microscraper.client.interfaces;

import java.util.Enumeration;

public interface Interfaces {
	public interface JSON {
		public interface Object {
			public abstract JSON.Array getJSONArray(String name) throws JSONInterfaceException;
			public abstract JSON.Object getJSONObject(String name) throws JSONInterfaceException;
			public abstract String getString(String name) throws JSONInterfaceException;
			public abstract int getInt(String name) throws JSONInterfaceException;
			public abstract boolean has(String name);
			public abstract boolean isNull(String name);
			public abstract Iterator keys();
			public abstract int length();
		}
		
		public interface Array {
			public abstract JSON.Array getJSONArray(int index) throws JSONInterfaceException;
			public abstract JSON.Object getJSONObject(int index) throws JSONInterfaceException;
			public abstract String getString(int index) throws JSONInterfaceException;
			public abstract String[] toArray() throws JSONInterfaceException; 
			public abstract int length();
		}
		public interface Tokener {
			public abstract Object nextValue() throws JSONInterfaceException;
		}
		public interface Iterator {
			public abstract boolean hasNext();
			public abstract java.lang.Object next();
		}
		public final class EnumerationIterator implements Iterator {
			private final Enumeration enumeration;
			public EnumerationIterator(Enumeration e) {
				enumeration = e;
			}
			
			@Override
			public boolean hasNext() {
				return enumeration.hasMoreElements();
			}

			@Override
			public java.lang.Object next() {
				return enumeration.nextElement();
			}
		}
		
		public abstract class JSONInterfaceException extends Exception {
			private static final long serialVersionUID = 1L;};
		
		public abstract Tokener getTokener(String jsonString);
	}


	/**
	 * Equivalent to a compiled java.util.regex.Pattern .
	 * @author john
	 *
	 */
	public interface Regexp {
		/**
		 * Equivalent to java.util.regex.Compile.
		 * @param patternString A pattern string to compile.
		 * @return A GeograpePattern.
		 */
		public abstract Pattern compile(String patternString);
		
		public interface Pattern {
			/**
			 * True/false based on whether we find a match.
			 * @param input
			 * @return Whether a match was found.
			 */
			public abstract boolean matches(String input);
			
			/**
			 * Return the match in the first set of parentheses, after a certain number of previous matches; if there are not parentheses,
			 * return the whole pattern.  Returns null if no match, or if there is no match at that matchNumber index.
			 * @param input
			 * @param matchNumber The number of matches to skip.
			 * @return The first grouped match, the entire match, or null.
			 */
			public abstract String match(String input, int matchNumber);
			
			/**
			 * Iterate through the input, and repeatedly provide the match from the first set
			 * of parentheses; if there are not parentheses, this becomes an alias for split(String input).
			 * @param input
			 * @return An array of all the first group matchings, or an array of all the matches,
			 * or null.
			 */
			public abstract String[] allMatches(String input);
		}
	}
	

	public interface SQL {
		public static interface Cursor {
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
		
		public abstract Cursor query(String sql) throws SQLInterfaceException;
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
	
	public interface Logger {
		/**
		 * Provide the ability to log errors.
		 */
		public abstract void e(String errorText, Throwable e);
		
		/**
		 * Provide the ability to log information.
		 */
		public abstract void i(String infoText);
	}
}
