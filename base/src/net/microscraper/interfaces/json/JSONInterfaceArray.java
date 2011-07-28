package net.microscraper.interfaces.json;

/**
 * Interface to a JSON array.
 * @author realest
 *
 */
public interface JSONInterfaceArray {
	/**
	 * Retrieve a {@link JSONInterfaceArray} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JSONInterfaceArray}.
	 * @throws JSONInterfaceException If the index does not exist, or its value is
	 *  not a JSON array.
	 */
	public abstract JSONInterfaceArray getJSONArray(int index) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JSONInterfaceArray}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JSONInterfaceArray}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>index</code> does not exist.
	 */
	public abstract boolean isJSONArray(int index) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link JSONInterfaceObject} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If the index does not exist, or its value is
	 *  not a JSON object.
	 * 
	 */
	public abstract JSONInterfaceObject getJSONObject(int index) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JSONInterfaceObject}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JSONInterfaceObject}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>index</code> does not exist.
	 */
	public abstract boolean isJSONObject(int index) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link String}.
	 * @throws JSONInterfaceException If the index does not exist, or its value is
	 *  not a {@link String}.
	 * 
	 */
	public abstract String getString(int index) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link String}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>index</code> does not exist.
	 */
	public abstract boolean isString(int index) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link int}.
	 * @throws JSONInterfaceException If the index does not exist, or its value is
	 *  not an {@link int}.
	 * 
	 */
	public abstract int getInt(int index) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>index</code> is an {@link int}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is an {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>index</code> does not exist.
	 */
	public abstract boolean isInt(int index) throws JSONInterfaceException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link boolean}.
	 * @throws JSONInterfaceException If the index does not exist, or its value is
	 *  not an {@link boolean}.
	 */
	public abstract boolean getBoolean(int index) throws JSONInterfaceException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link boolean}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JSONInterfaceException If <code>index</code> does not exist.
	 */
	public abstract boolean isBoolean(int index) throws JSONInterfaceException;
	
	/**
	 * Return a copy of the {@link JSONInterfaceArray} as an array of strings, if it comprises
	 * {@link String} elements exclusively.
	 * @return An array of {@link String}s.
	 * @throws JSONInterfaceException If the {@link JSONInterfaceArray} has a non-{@link String}
	 * element.
	 */
	public abstract String[] toArray() throws JSONInterfaceException; 
	
	/**
	 * 
	 * @return The length of the {@link JSONInterfaceArray}.
	 */
	public abstract int length();
	
	/**
	 * 
	 * @return The {@link JSONInterfaceArray}'s {@link JSONLocation}.
	 */
	public abstract JSONLocation getLocation();
}