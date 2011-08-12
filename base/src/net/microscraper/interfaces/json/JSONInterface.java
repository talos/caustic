package net.microscraper.interfaces.json;

import net.microscraper.interfaces.uri.URIInterface;
import net.microscraper.interfaces.uri.URIInterfaceException;

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
public interface JSONInterface {
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
	 * Load a {@link JSONInterfaceObject} from a {@link URIInterface}.
	 * @param location The {@link URIInterface} URI to load.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If there is an error generating
	 * the {@link JSONInterfaceObject}.
	 * @throws URIInterfaceException if the {@link URIInterface} could not be resolved.
	 */
	public abstract JSONInterfaceObject load(URIInterface location) 
			throws JSONInterfaceException, URIInterfaceException;
	
	/**
	 * Compile a {@link JSONInterfaceObject} directly from a {@link String}.
	 * @param location The {@link URIInterface} URI to use when resolving <code>jsonString</code>'s references.
	 * @param jsonString The {@link String} to parse.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws JSONInterfaceException If there is an error generating
	 * the {@link JSONInterfaceObject}.
	 * @throws URIInterfaceException if the {@link URIInterface} could not be resolved.
	 */
	public abstract JSONInterfaceObject parse(URIInterface location, String jsonString)
			throws JSONInterfaceException, URIInterfaceException;
}