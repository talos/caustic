package net.microscraper.interfaces.json;

/**
 * Interface to a JSON object.  Should be initialized by {@link JSONInterface#load(JSONLocation)}.
 * @see JSONInterface 
 * @see JSONLocation
 * @author realest
 *
 */
public interface JSONInterfaceObject {
	/**
	 * Retrieve a {@link JSONInterfaceArray} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONInterfaceArray}.
	 * @throws JSONInterfaceException If <code>key</code> does not exist, or its value is
	 * not a JSON array.
	 */
	public abstract JSONInterfaceArray getJSONArray(String key) throws JSONInterfaceException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JSONInterfaceArray}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JSONInterfaceArray}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>key</code> does not exist.
	 */
	public abstract boolean isJSONArray(String key) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link JSONInterfaceObject} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If <code>key</code> does not exist, or its value is
	 * not a JSON object.
	 */
	public abstract JSONInterfaceObject getJSONObject(String key) throws JSONInterfaceException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JSONInterfaceObject}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JSONInterfaceObject}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>key</code> does not exist.
	 */
	public abstract boolean isJSONObject(String key) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If <code>key</code> does not exist, or its value is
	 * not a {@link String}.
	 */
	public abstract String getString(String key) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link String}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>key</code> does not exist.
	 */
	public abstract boolean isString(String key) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If <code>key</code> does not exist, or its value is
	 * not a {@link int}.
	 */
	public abstract int getInt(String key) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link int}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>key</code> does not exist.
	 */
	public abstract boolean isInt(String key) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If <code>key</code> does not exist, or its value is
	 * not a {@link boolean}.
	 */
	public abstract boolean getBoolean(String key) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link boolean}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>key</code> does not exist.
	 */
	public abstract boolean isBoolean(String key) throws JSONInterfaceException;
	
	/**
	 * Determines whether this {@link JSONInterfaceObject} contains a value for a <code>key</code>.
	 * This is not the same as {@link #isNull}, which checks whether <code>key</code> is <code>
	 * null</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JSONInterfaceObject} contains a value for the <code>
	 * key</code>, false otherwise.
	 * @see #isNull(String)
	 */
	public abstract boolean has(String key);

	/**
	 * Determines whether this {@link JSONInterfaceObject} contains a <code>null</code>
	 * value for a <code>key</code>.  This is not the same as {@link #has(String)}, which
	 * checks for the existence of a <code>key</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JSONInterfaceObject} contains a <code>null</code>
	 * value, <code>false</code> otherwise.
	 * @see #has(String)
	 */
	public abstract boolean isNull(String key);
	
	/**
	 * 
	 * @return A {@link JSONInterfaceIterator} to iterate over this {@link JSONInterfaceObject}'s
	 * keys.
	 */
	public abstract JSONInterfaceIterator keys();
	
	/**
	 * 
	 * @return The number of key-value mappings in this {@link JSONInterfaceObject}.
	 */
	public abstract int length();
	
	/**
	 * 
	 * @return The {@link JSONInterfaceObject}'s {@link JSONLocation}.
	 */
	public abstract JSONLocation getLocation();
}