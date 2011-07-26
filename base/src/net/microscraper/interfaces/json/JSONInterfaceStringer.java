package net.microscraper.interfaces.json;

/**
 * An interface for constructing new JSON objects.  Indebted to {@link org.json.me}.
 * @author realest
 * @see org.json.me.JSONStringer
 * @see org.json.me.JSONWriter
 *
 */
public interface JSONInterfaceStringer {
	
	/**
	 * Begin an array.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If an array cannot be inserted at this point.
	 * @see #endArray()
	 * @see #value(boolean)
	 * @see #value(double)
	 * @see #value(long)
	 * @see #value(String)
	 */
    public JSONInterfaceStringer array() throws JSONInterfaceException;
    
	/**
	 * End an array.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If there is no array to end at this point.
	 * @see #array()
	 */
    public JSONInterfaceStringer endArray() throws JSONInterfaceException;
    
	/**
	 * End an object.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If there was no object to end at this point.
	 * @see #object()
	 */
    public JSONInterfaceStringer endObject() throws JSONInterfaceException;
    
    /**
	 * Insert a key.
	 * @param The {@link String} key name.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If a key cannot be inserted at this point.
	 * @see #object()
	 * @see #value(boolean)
	 * @see #value(double)
	 * @see #value(long)
	 * @see #value(String)
	 */
    public JSONInterfaceStringer key(String s) throws JSONInterfaceException;
    
	/**
	 * Begin an object.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If an object cannot be inserted at this point.
	 * @see #key(String)
	 * @see #endObject()
	 */
    public JSONInterfaceStringer object() throws JSONInterfaceException;
    

	/**
	 * Insert a {@link String} value.
	 * @param The {@link String} value to insert.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If a value cannot be inserted at this point; this 
	 * would be the case if this is not called inside an array or immediately after 
	 * {@link #key}.
	 * @see #array()
	 * @see #key(String)
	 * @see #endObject()
	 */
    public JSONInterfaceStringer value(String s) throws JSONInterfaceException;

	/**
	 * Insert a {@link boolean} value.
	 * @param The {@link boolean} value to insert.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If a value cannot be inserted at this point; this 
	 * would be the case if this is not called inside an array or immediately after 
	 * {@link #key}.
	 * @see #array()
	 * @see #key(String)
	 * @see #endObject()
	 */
    public JSONInterfaceStringer value(boolean b) throws JSONInterfaceException;
    

	/**
	 * Insert a {@link double} value.
	 * @param The {@link double} value to insert.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If a value cannot be inserted at this point; this 
	 * would be the case if this is not called inside an array or immediately after 
	 * {@link #key}.
	 * @see #array()
	 * @see #key(String)
	 * @see #endObject()
	 */
    public JSONInterfaceStringer value(double d) throws JSONInterfaceException;

	/**
	 * Insert a {@link long} value.
	 * @param The {@link long} value to insert.
	 * @return The {@link JSONInterfaceStringer} for chainability.
	 * @throws JSONInterfaceException If a value cannot be inserted at this point; this 
	 * would be the case if this is not called inside an array or immediately after 
	 * {@link #key}.
	 * @see #array()
	 * @see #key(String)
	 * @see #endObject()
	 */
    public JSONInterfaceStringer value(long l) throws JSONInterfaceException;

	/**
	 * The {@link JSONInterfaceStringer} up to the current point as a {@link String}.
	 * @return A {@link String}.
	 * @see #endArray()
	 * @see #endObject()
	 */
    public String toString();
}