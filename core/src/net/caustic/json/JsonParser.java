package net.caustic.json;

import java.util.Hashtable;

/**
 * Implementations provide an interface for parsing JSON objects.  The format
 * of this interface is indebted to org.json.me.
 * @author john
 *
 */
public interface JsonParser {
	
	/**
	 * Determine with a {@link String} could be parsed into a {@link JsonObject}
	 * using {@link #newObject(String)}.
	 * @param string The {@link String} to test.
	 * @return <code>true</code> if <code>string</code> could be parsed, <code>
	 * false</code> otherwise.
	 */
	public abstract boolean isJsonObject(String string);

	/**
	 * Determine with a {@link String} could be parsed into a {@link JsonArray}
	 * using {@link #newObject(String)}.
	 * @param string The {@link String} to test.
	 * @return <code>true</code> if <code>string</code> could be parsed, <code>
	 * false</code> otherwise.
	 */
	public abstract boolean isJsonArray(String string);
	
	/**
	 * Compile a {@link JsonObject} directly from a {@link String}.
	 * @param jsonString The {@link String} to parse.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 */
	public abstract JsonObject newObject(String jsonString) throws JsonException;
	
	/**
	 * Compile a flat {@link JsonObject} from a {@link Hashtable} of
	 * {@link String} to {@link String} mappings.
	 * @param map A {@link Hashtable} of {@link String} to {@link String} mappings.
	 * @return A flat {@link JsonObject} with the same mappings as <code>hash</code>.
	 * @throws JsonException if <code>hash</code> could not be converted into a flat
	 * {@link JsonObject}.
	 */
	public abstract JsonObject generate(Hashtable map) throws JsonException;
	

	/**
	 * Compile a {@link JsonArray} directly from a {@link String}.
	 * @param jsonString The {@link String} to parse.
	 * @return A {@link JsonArray}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 */
	public abstract JsonArray newArray(String jsonString) throws JsonException;
}