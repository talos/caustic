package net.microscraper.json;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.Uri;

/**
 * Implementations provide a fully-featured interface for microscraper to
 * handle JSON with references.  The format
 * of this interface is indebted to org.json.me, but also should implement
 * JSON referencing when the object is first initialized.
 * @author john
 * @see #REFERENCE_KEY
 * @see #EXTENDS
 *
 */
public interface JsonParser {
	/**
	 * When the parser encounters this as a key in an object, it should replace
	 * the object with the contents of the JSON loaded from the URI that is this
	 * key's value.
	 */
	public static final String REFERENCE_KEY = "$ref";
	
	/**
	 * When the parser encounters this as a key in an object, it should append 
	 * the key-value pairs of the value object into the containing object.  If
	 * the value is an array, it should append all of the key-value pairs of
	 * each array element into the original object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Load a {@link JsonObject} from a {@link Uri}.
	 * @param location The {@link Uri} URI to load.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 * @throws IOException if the <code>location</code> could not be loaded, or one
	 * of the {@link JsonObject}'s references could not be.
	 * @throws MalformedUriException if a reference could not be resolved.
	 */
	public abstract JsonObject load(Uri location) 
			throws JsonException, IOException, MalformedUriException;
	
	/**
	 * Parse a {@link JsonObject} directly from a {@link String}.  References should
	 * be followed from the directory of execution.
	 * @param jsonString The {@link String} to parse.
	 * @return A {@link JsonObject}.
	 * @throws JsonException If there is an error generating
	 * the {@link JsonObject}.
	 * @throws IOException if one of the {@link JsonObject}'s references could not
	 * be loaded.
	 * @throws MalformedUriException if a reference could not be resolved.
	 */
	public abstract JsonObject parse(String jsonString)
			throws JsonException, IOException, MalformedUriException;
	
	/**
	 * Compile a flat {@link JsonObject} from a {@link Hashtable} of
	 * {@link String} to {@link String} mappings.
	 * @param map A {@link Hashtable} of {@link String} to {@link String} mappings.
	 * @return A flat {@link JsonObject} with the same mappings as <code>hash</code>.
	 */
	public abstract JsonObject generate(Hashtable map) throws JsonException;
}