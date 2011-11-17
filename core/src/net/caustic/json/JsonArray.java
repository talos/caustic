package net.caustic.json;

/**
 * Interface to a JSON array.
 * @author realest
 *
 */
public interface JsonArray {
	/**
	 * Retrieve a {@link JsonArray} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JsonArray}.
	 * @throws JsonException If the index does not exist, or its value is
	 *  not a JSON array.
	 */
	public abstract JsonArray getJsonArray(int index) throws JsonException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JsonArray}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JsonArray}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>index</code> does not exist.
	 */
	public abstract boolean isJsonArray(int index) throws JsonException;
	
	/**
	 * Retrieve a {@link JsonObject} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If the index does not exist, or its value is
	 *  not a JSON object.
	 * 
	 */
	public abstract JsonObject getJsonObject(int index) throws JsonException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link JsonObject}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link JsonObject}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>index</code> does not exist.
	 */
	public abstract boolean isJsonObject(int index) throws JsonException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link String}.
	 * @throws JsonException If the index does not exist, or its value is
	 *  not a {@link String}.
	 * 
	 */
	public abstract String getString(int index) throws JsonException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link String}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>index</code> does not exist.
	 */
	public abstract boolean isString(int index) throws JsonException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link int}.
	 * @throws JsonException If the index does not exist, or its value is
	 *  not an {@link int}.
	 * 
	 */
	public abstract int getInt(int index) throws JsonException;

	/**
	 * Determine whether the specified <code>index</code> is an {@link int}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is an {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>index</code> does not exist.
	 */
	public abstract boolean isInt(int index) throws JsonException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>index</code>.
	 * @param index The <code>int</code> index to retrieve.
	 * @return A {@link boolean}.
	 * @throws JsonException If the index does not exist, or its value is
	 *  not an {@link boolean}.
	 */
	public abstract boolean getBoolean(int index) throws JsonException;

	/**
	 * Determine whether the specified <code>index</code> is a {@link boolean}.
	 * @param index The {@link int} key to check.
	 * @return <code>true</code> if <code>index</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>index</code> does not exist.
	 */
	public abstract boolean isBoolean(int index) throws JsonException;
	
	/**
	 * Return a copy of the {@link JsonArray} as an array of strings, if it comprises
	 * {@link String} elements exclusively.
	 * @return An array of {@link String}s.
	 * @throws JsonException If the {@link JsonArray} has a non-{@link String}
	 * element.
	 */
	public abstract String[] toArray() throws JsonException; 
	
	/**
	 * 
	 * @return The length of the {@link JsonArray}.
	 */
	public abstract int length();
}