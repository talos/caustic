package net.microscraper.interfaces.json;

import java.io.IOException;

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
	 * the key-value pairs of the value object into the containing object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Load a {@link JSONInterfaceObject} from a {@link JSONLocation jsonLocation}
	 * @param location The {@link JSONLocation} to load.
	 * @return A {@link JSONInterfaceObject}.
	 * @throws IOException If there is an error loading from <code>location</code>
	 * or one of its references.
	 * @throws JSONInterfaceException If there is an error generating
	 * the {@link JSONInterfaceObject}.
	 * @throws JSONLocationException 
	 */
	public abstract JSONInterfaceObject load(JSONLocation location) 
			throws IOException, JSONInterfaceException, JSONLocationException;
}