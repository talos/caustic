package net.microscraper.interfaces.json;

import java.util.Enumeration;

/**
 * Implementations provide a fully-featured interface for microscraper to
 * handle JSON with references.  The format
 * of this interface is indebted to org.json.me, but also should implement
 * JSON referencing.
 * @author john
 *
 */
public interface JSONInterface {
	/**
	 * When the parser encounters this as a key in an object, it should replace
	 * the object with the contents of the JSON loaded from the URI that is this
	 * key's value.
	 */
	public static final String REFERENCE_KEY = "$ref";
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
	
	/**
	 * Load a {@link JSONInterfaceObject} from a {@link JSONLocation jsonLocation}
	 * @param jsonLocation The {@link JSONLocation} to load.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If there is an error generating
	 * the {@link JSONInterfaceObject}.
	 */
	public abstract JSONInterfaceObject load(JSONLocation location) throws JSONInterfaceException;
	
	public abstract JSONInterfaceStringer getStringer() throws JSONInterfaceException;
}