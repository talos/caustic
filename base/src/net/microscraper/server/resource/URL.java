package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Resource;

/**
 * The {@link URL} {@link Resource} holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL extends Resource {	
	/**
	 * A string that can be mustached and used as a URL.
	 */
	public final MustacheTemplate url;
	
	private static final String URL = "url";

	/**
	 * Deserialize a {@link URL} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link URL} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link URL}.
	 */
	public URL(JSONInterfaceObject jsonObject) throws DeserializationException {
		super(jsonObject.getLocation());
		try {
			this.url = new MustacheTemplate(jsonObject.getString(URL));
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
