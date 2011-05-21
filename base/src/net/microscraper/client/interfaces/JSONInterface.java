package net.microscraper.client.interfaces;

import java.util.Enumeration;

/**
 * Implementations provide a fully-featured interface for microscraper to do JSON.  The format
 * of this interface is indebted to org.json.me.
 * @author john
 *
 */
public interface JSONInterface {
	public static interface JSONInterfaceTokener {
		public abstract JSONInterfaceObject nextValue() throws JSONInterfaceException;
	}
	public static interface JSONInterfaceWriter {
	    public JSONInterface.JSONInterfaceWriter array() throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter endArray() throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter endObject() throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter key(String s) throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter object() throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter value(String s) throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter value(boolean b) throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter value(double d) throws JSONInterfaceException;
	    public JSONInterface.JSONInterfaceWriter value(long l) throws JSONInterfaceException;
	}
	public static final class EnumerationIterator implements JSONInterfaceIterator {
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
	
	public abstract JSONInterface.JSONInterfaceTokener getTokener(String jsonString) throws JSONInterfaceException;
	public abstract JSONInterfaceStringer getStringer() throws JSONInterfaceException;
}