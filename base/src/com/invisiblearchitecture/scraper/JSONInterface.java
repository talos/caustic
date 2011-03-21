package com.invisiblearchitecture.scraper;

import java.util.Enumeration;

public interface JSONInterface {
	public interface JSONInterfaceObject {
		public abstract JSONInterfaceArray getJSONArray(String name) throws JSONInterfaceException;
		public abstract JSONInterfaceObject getJSONObject(String name) throws JSONInterfaceException;
		public abstract String getString(String name) throws JSONInterfaceException;
		public abstract int getInt(String name) throws JSONInterfaceException;
		public abstract boolean has(String name);
		public abstract boolean isNull(String name);
		public abstract IteratorInterface keys();
		public abstract int length();
	}
	
	public interface JSONInterfaceArray {
		public abstract JSONInterfaceArray getJSONArray(int index) throws JSONInterfaceException;
		public abstract JSONInterfaceObject getJSONObject(int index) throws JSONInterfaceException;
		public abstract String getString(int index) throws JSONInterfaceException;
		public abstract String[] toArray() throws JSONInterfaceException; 
		public abstract int length();
	}
	public interface JSONInterfaceTokener {
		public abstract JSONInterfaceObject nextValue() throws JSONInterfaceException;
	}
	public interface IteratorInterface {
		public abstract boolean hasNext();
		public abstract Object next();
	}
	public final class EnumerationIterator implements IteratorInterface {
		private final Enumeration enumeration;
		public EnumerationIterator(Enumeration e) {
			enumeration = e;
		}
		
		@Override
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		@Override
		public Object next() {
			return enumeration.nextElement();
		}
		
	}
	
	public abstract class JSONInterfaceException extends Exception {
		private static final long serialVersionUID = 1L;};
	
	public abstract JSONInterfaceTokener getTokener(String jsonString);
}
