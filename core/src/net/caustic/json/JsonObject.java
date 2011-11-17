package net.caustic.json;

/**
 * Interface to a JSON object.
 * @see JsonParser 
 * @author realest
 *
 */
public interface JsonObject {
	/**
	 * Retrieve a {@link JsonArray} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JsonArray}.
	 * @throws JsonException If <code>key</code> does not exist, or its value is
	 * not a JSON array.
	 */
	public abstract JsonArray getJsonArray(String key) throws JsonException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JsonArray}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JsonArray}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>key</code> does not exist.
	 */
	public abstract boolean isJsonArray(String key) throws JsonException;
	
	/**
	 * Retrieve a {@link JsonObject} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If <code>key</code> does not exist, or its value is
	 * not a JSON object.
	 */
	public abstract JsonObject getJsonObject(String key) throws JsonException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JsonObject}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JsonObject}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>key</code> does not exist.
	 */
	public abstract boolean isJsonObject(String key) throws JsonException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If <code>key</code> does not exist, or its value is
	 * not a {@link String}.
	 */
	public abstract String getString(String key) throws JsonException;

	/**
	 * Determine whether the specified <code>key</code>'s value could be a {@link String}.
	 * Will still return <code>true</code> if the value is a {@link int} or {@link
	 * boolean}, as they can be expressed as a {@link String}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> could be a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>key</code> does not exist.
	 */
	public abstract boolean isString(String key) throws JsonException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If <code>key</code> does not exist, or its value is
	 * not a {@link int}.
	 */
	public abstract int getInt(String key) throws JsonException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link int}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>key</code> does not exist.
	 */
	public abstract boolean isInt(String key) throws JsonException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If <code>key</code> does not exist, or its value is
	 * not a {@link boolean}.
	 */
	public abstract boolean getBoolean(String key) throws JsonException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link boolean}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JsonException If <code>key</code> does not exist.
	 */
	public abstract boolean isBoolean(String key) throws JsonException;
	
	/**
	 * Determines whether this {@link JsonObject} contains a value for a <code>key</code>.
	 * This is not the same as {@link #isNull}, which checks whether <code>key</code> is <code>
	 * null</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JsonObject} contains a value for the <code>
	 * key</code>, false otherwise.
	 * @see #isNull(String)
	 */
	public abstract boolean has(String key);

	/**
	 * Determines whether this {@link JsonObject} contains a <code>null</code>
	 * value for a <code>key</code>.  This is not the same as {@link #has(String)}, which
	 * checks for the existence of a <code>key</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JsonObject} contains a <code>null</code>
	 * value, <code>false</code> otherwise.
	 * @see #has(String)
	 */
	public abstract boolean isNull(String key);
	
	/**
	 * 
	 * @return A {@link JsonIterator} to iterate over this {@link JsonObject}'s
	 * keys.
	 */
	public abstract JsonIterator keys();
	
	/**
	 * 
	 * @return The number of key-value mappings in this {@link JsonObject}.
	 */
	public abstract int length();
}