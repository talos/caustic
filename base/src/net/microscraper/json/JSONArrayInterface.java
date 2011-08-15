package net.microscraper.json;

/**
 * Interface to a JSON array.
 * @author realest
 *
 */
public interface JSONArrayInterface {
	/**
	 * Retrieve a {@link JSONArrayInterface} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JSONArrayInterface}.
	 * @throws JSONParserException If the index does not exist, or its value is
	 *  not a JSON array.
	 */
	public abstract JSONArrayInterface getJSONArray(int index) throws JSONParserException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JSONArrayInterface}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JSONArrayInterface}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>index</code> does not exist.
	 */
	public abstract boolean isJSONArray(int index) throws JSONParserException;
	
	/**
	 * Retrieve a {@link JSONObjectInterface} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JSONObjectInterface}.
	 * @throws JSONParserException If the index does not exist, or its value is
	 *  not a JSON object.
	 * 
	 */
	public abstract JSONObjectInterface getJSONObject(int index) throws JSONParserException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JSONObjectInterface}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JSONObjectInterface}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>index</code> does not exist.
	 */
	public abstract boolean isJSONObject(int index) throws JSONParserException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link String}.
	 * @throws JSONParserException If the index does not exist, or its value is
	 *  not a {@link String}.
	 * 
	 */
	public abstract String getString(int index) throws JSONParserException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link String}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>index</code> does not exist.
	 */
	public abstract boolean isString(int index) throws JSONParserException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link int}.
	 * @throws JSONParserException If the index does not exist, or its value is
	 *  not an {@link int}.
	 * 
	 */
	public abstract int getInt(int index) throws JSONParserException;

	/**
	 * Determine whether the specified <code>index</code> is an {@link int}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is an {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>index</code> does not exist.
	 */
	public abstract boolean isInt(int index) throws JSONParserException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link boolean}.
	 * @throws JSONParserException If the index does not exist, or its value is
	 *  not an {@link boolean}.
	 */
	public abstract boolean getBoolean(int index) throws JSONParserException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link boolean}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>index</code> does not exist.
	 */
	public abstract boolean isBoolean(int index) throws JSONParserException;
	
	/**
	 * Return a copy of the {@link JSONArrayInterface} as an array of strings, if it comprises
	 * {@link String} elements exclusively.
	 * @return An array of {@link String}s.
	 * @throws JSONParserException If the {@link JSONArrayInterface} has a non-{@link String}
	 * element.
	 */
	public abstract String[] toArray() throws JSONParserException; 
	
	/**
	 * 
	 * @return The length of the {@link JSONArrayInterface}.
	 */
	public abstract int length();
	
	/**
	 * 
	 * @return The {@link JSONInterfaceArray}'s {@link JSONLocation}.
	 */
	//public abstract JSONLocation getLocation();
}