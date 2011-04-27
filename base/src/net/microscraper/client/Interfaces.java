package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;

public class Interfaces {
	public static interface JSON {
		
		
		public static interface Object {
			public abstract JSON.Array getJSONArray(String name) throws JSONInterfaceException;
			public abstract JSON.Object getJSONObject(String name) throws JSONInterfaceException;
			public abstract java.lang.Object get(String name) throws JSONInterfaceException;
			public abstract String getString(String name) throws JSONInterfaceException;
			public abstract int getInt(String name) throws JSONInterfaceException;
			public abstract boolean has(String name);
			public abstract boolean isNull(String name);
			public abstract Iterator keys();
			public abstract int length();
		}
		
		public static interface Array {
			public abstract JSON.Array getJSONArray(int index) throws JSONInterfaceException;
			public abstract JSON.Object getJSONObject(int index) throws JSONInterfaceException;
			public abstract java.lang.Object get(int index) throws JSONInterfaceException;
			public abstract String getString(int index) throws JSONInterfaceException;
			public abstract String[] toArray() throws JSONInterfaceException; 
			public abstract int length();
		}/*
		public static interface Null {
			public abstract boolean equals(Object obj);
			public abstract String toString();
		}*/
		public static interface Tokener {
			public abstract Object nextValue() throws JSONInterfaceException;
		}
		public static interface Iterator {
			public abstract boolean hasNext();
			public abstract java.lang.Object next();
		}
		public static final class EnumerationIterator implements Iterator {
			private final Enumeration enumeration;
			public EnumerationIterator(Enumeration e) {
				enumeration = e;
			}
			
			public boolean hasNext() {
				return enumeration.hasMoreElements();
			}

			public java.lang.Object next() {
				return enumeration.nextElement();
			}
		}
		
		public static class JSONInterfaceException extends Exception {
			private static final long serialVersionUID = 1L;
			public JSONInterfaceException(String message ) {super(message); }
			public JSONInterfaceException(Throwable e ) {super(e); }
		};

		
		public abstract Tokener getTokener(String jsonString) throws JSONInterfaceException;
		public abstract String toJSON(Hashtable hashtable) throws JSONInterfaceException;
		//public abstract Null getNull();
	}


	/**
	 * Equivalent to a compiled java.util.regex.Pattern .
	 * @author john
	 *
	 */
	public static interface Regexp {
		/**
		 * Equivalent to java.util.regex.Compile.
		 * @param patternString A pattern string to compile.
		 * @return A GeograpePattern.
		 */
		public abstract Pattern compile(String patternString);
		
		public static interface Pattern {
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
			public abstract String match(String input, int matchNumber) throws NoMatches;
			
			/**
			 * Iterate through the input, and repeatedly provide the match from the first set
			 * of parentheses; if there are not parentheses, this becomes an alias for split(String input).
			 * @param input
			 * @return An array of all the first group matchings, or an array of all the matches.
			 */
			public abstract String[] allMatches(String input) throws NoMatches;
		}
		
		public static class NoMatches extends Exception {
			
			public NoMatches(Pattern pattern, String string) {
				super(pattern.toString() + " did not match against " + string);
			}
			/**
			 * 
			 */
			private static final long serialVersionUID = -1808377327875482874L;
			
		}
	}
	
	public static interface Logger {
		/**
		 * Provide the ability to log throwables as errors.
		 */
		public abstract void e(Throwable e);
		
		/**
		 * Provide the ability to log throwables as warnings.
		 */
		public abstract void w(Throwable w);
		
		/**
		 * Provide the ability to log information.
		 */
		public abstract void i(String infoText);
	}
}
