package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.client.Interfaces.Regexp.Pattern;

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
		}
		public static interface Tokener {
			public abstract Object nextValue() throws JSONInterfaceException;
		}
		public static interface Iterator {
			public abstract boolean hasNext();
			public abstract java.lang.Object next();
		}
		public static interface Writer {
		    public Writer array() throws JSONInterfaceException;
		    public Writer endArray() throws JSONInterfaceException;
		    public Writer endObject() throws JSONInterfaceException;
		    public Writer key(String s) throws JSONInterfaceException;
		    public Writer object() throws JSONInterfaceException;
		    public Writer value(String s) throws JSONInterfaceException;
		    public Writer value(boolean b) throws JSONInterfaceException;
		    public Writer value(double d) throws JSONInterfaceException;
		    public Writer value(long l) throws JSONInterfaceException;
		}
		public static interface Stringer extends Writer {
		    public String toString();
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
		public abstract Stringer getStringer() throws JSONInterfaceException;
		public abstract String toJSON(Hashtable hashtable) throws JSONInterfaceException;
	}


	/**
	 * Equivalent to a compiled java.util.regex.Pattern .
	 * @author john
	 *
	 */
	public static interface Regexp {
		/**
		 * Equivalent to java.util.regex.Compile
		 * @param attributeValue
		 * @param isCaseInsensitive
		 * @param isMultiline
		 * @param doesDotMatchNewline
		 * @return
		 */
		public abstract Pattern compile(String attributeValue,
				boolean isCaseInsensitive, boolean isMultiline,
				boolean doesDotMatchNewline);
		
		public static interface Pattern {
			/**
			 * True/false based on whether we find a match.
			 * @param input
			 * @return Whether a match was found.
			 */
			public abstract boolean matches(String input);
			public abstract boolean matches(String input, int matchNumber);
			
			public abstract String match(String input, String substitution, int matchNumber) throws NoMatches, MissingGroup;
			public abstract String[] allMatches(String input, String substitution) throws NoMatches, MissingGroup;
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
		public static class MissingGroup extends Exception {
			
			public MissingGroup(Pattern pattern, int group) {
				super(pattern.toString() + " did not contain a group " + Integer.toString(group));
			}
			/**
			 * 
			 */
			private static final long serialVersionUID = -1808377327875482874L;
			
		}
		
	}
	
	public static interface Logger {
		public static final int MAX_ENTRY_LENGTH = 512;
		
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
