package net.microscraper.client.interfaces;

import java.util.Enumeration;

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
