package net.microscraper.interfaces.json;

import java.io.IOException;

/**
 * Implementations provide a fully-featured interface for microscraper to
 * handle JSON with references.  The format
 * of this interface is indebted to org.json.me, but also should implement
 * JSON referencing when the object is first initialized.
 * @author john
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
	 * Load a {@link JSONInterfaceObject} from a {@link JSONLocation jsonLocation}
	 * @param jsonLocation The {@link String} URI to load.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws IOException 
	 * @throws JSONInterfaceException If there is an error generating
	 * the {@link JSONInterfaceObject}.
	 * @throws JSONLocationException 
	 */
	public abstract JSONInterfaceObject load(JSONLocation location) throws IOException, JSONInterfaceException, JSONLocationException;
}