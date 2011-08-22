package net.microscraper.json;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.uri.Uri;
import net.microscraper.uri.MalformedUriException;;

/**
 * Implementations provide an interface for parsing JSON objects.  The format
 * of this interface is indebted to org.json.me.
 * @author john
 *
 */
public interface JsonParser {
	/**
	 * When the parser encounters this as a key in an object, it should replace
	 * the object with the contents of the JSON loaded from the URI that is this
	 * key's value.
	 */
	//public static final String REFERENCE_KEY = "$ref";
	
	/**
	 * When the parser encounters this as a key in an object, it should append 
	 * the key-value pairs of the value object into the containing object.  If
	 * the value is an array, it should append all of the key-value pairs of
	 * each array element into the original object.
	 */
	//public static final String EXTENDS = "extends";
	
	/**
	 * Load a {@link JsonObject} from a {@link Uri}.
	 * @param uriString The {@link String} URI to load.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 * @throws MalformedUriException if the {@link Uri} could not be resolved.
	 * @throws IOException if a reference could not be loaded.
	 */
	/*public abstract JsonObject load(String uriString) 
			throws JsonException, MalformedUriException, IOException;*/
	
	/**
	 * Compile a {@link JsonObject} directly from a {@link String}.
	 * @param jsonString The {@link String} to parse.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 */
	public abstract JsonObject parse(String jsonString) throws JsonException;
	//		throws JsonException, MalformedUriException, IOException;
	
	/**
	 * Compile a flat {@link JsonObject} from a {@link Hashtable} of
	 * {@link String} to {@link String} mappings.
	 * @param map A {@link Hashtable} of {@link String} to {@link String} mappings.
	 * @return A flat {@link JsonObject} with the same mappings as <code>hash</code>.
	 * @throws JsonException if <code>hash</code> could not be converted into a flat
	 * {@link JsonObject}.
	 */
	public abstract JsonObject generate(Hashtable map) throws JsonException;
}