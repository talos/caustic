package net.microscraper.json;

import net.microscraper.uri.URIInterface;

/**
 * Interface to a JSON object.  Should be initialized by {@link JSONParser#load(URIInterface)}.
 * @see JSONParser 
 * @see URIInterface
 * @author realest
 *
 */
public interface JSONObjectInterface {
	/**
	 * Retrieve a {@link JSONArrayInterface} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONArrayInterface}.
	 * @throws JSONParserException If <code>key</code> does not exist, or its value is
	 * not a JSON array.
	 */
	public abstract JSONArrayInterface getJSONArray(String key) throws JSONParserException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JSONArrayInterface}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JSONArrayInterface}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>key</code> does not exist.
	 */
	public abstract boolean isJSONArray(String key) throws JSONParserException;
	
	/**
	 * Retrieve a {@link JSONObjectInterface} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONObjectInterface}.
	 * @throws JSONParserException If <code>key</code> does not exist, or its value is
	 * not a JSON object.
	 */
	public abstract JSONObjectInterface getJSONObject(String key) throws JSONParserException;
	
	/**
	 * Determine whether the specified <code>key</code> is a {@link JSONObjectInterface}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link JSONObjectInterface}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>key</code> does not exist.
	 */
	public abstract boolean isJSONObject(String key) throws JSONParserException;
	
	/**
	 * Retrieve a {@link String} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONObjectInterface}.
	 * @throws JSONParserException If <code>key</code> does not exist, or its value is
	 * not a {@link String}.
	 */
	public abstract String getString(String key) throws JSONParserException;

	/**
	 * Determine whether the specified <code>key</code>'s value could be a {@link String}.
	 * Will still return <code>true</code> if the value is a {@link int} or {@link
	 * boolean}, as they can be expressed as a {@link String}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> could be a {@link String}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>key</code> does not exist.
	 */
	public abstract boolean isString(String key) throws JSONParserException;
	
	/**
	 * Retrieve a {@link int} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONObjectInterface}.
	 * @throws JSONParserException If <code>key</code> does not exist, or its value is
	 * not a {@link int}.
	 */
	public abstract int getInt(String key) throws JSONParserException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link int}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link int}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>key</code> does not exist.
	 */
	public abstract boolean isInt(String key) throws JSONParserException;
	
	/**
	 * Retrieve a {@link boolean} from the specified <code>key</code>.
	 * @param key The {@link String} key to retrieve.
	 * @return A {@link JSONObjectInterface}.
	 * @throws JSONParserException If <code>key</code> does not exist, or its value is
	 * not a {@link boolean}.
	 */
	public abstract boolean getBoolean(String key) throws JSONParserException;

	/**
	 * Determine whether the specified <code>key</code> is a {@link boolean}.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if <code>key</code> is a {@link boolean}, 
	 * <code>false</code> otherwise.
	 * @throws JSONParserException If <code>key</code> does not exist.
	 */
	public abstract boolean isBoolean(String key) throws JSONParserException;
	
	/**
	 * Determines whether this {@link JSONObjectInterface} contains a value for a <code>key</code>.
	 * This is not the same as {@link #isNull}, which checks whether <code>key</code> is <code>
	 * null</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JSONObjectInterface} contains a value for the <code>
	 * key</code>, false otherwise.
	 * @see #isNull(String)
	 */
	public abstract boolean has(String key);

	/**
	 * Determines whether this {@link JSONObjectInterface} contains a <code>null</code>
	 * value for a <code>key</code>.  This is not the same as {@link #has(String)}, which
	 * checks for the existence of a <code>key</code>.
	 * @param key The {@link String} key to check.
	 * @return <code>true</code> if the {@link JSONObjectInterface} contains a <code>null</code>
	 * value, <code>false</code> otherwise.
	 * @see #has(String)
	 */
	public abstract boolean isNull(String key);
	
	/**
	 * 
	 * @return A {@link JSONIterator} to iterate over this {@link JSONObjectInterface}'s
	 * keys.
	 */
	public abstract JSONIterator keys();
	
	/**
	 * 
	 * @return The number of key-value mappings in this {@link JSONObjectInterface}.
	 */
	public abstract int length();
	
	/**
	 * 
	 * @return The {@link JSONInterfaceObject}'s {@link JSONLocation}.
	 */
	//public abstract JSONLocation getLocation();
}