package net.microscraper.server.resource;

import java.io.IOException;

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
public class URL extends Scraper {	
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
	 * @throws IOException If there was an error loading a referenced {@link Resource}.
	 */
	public URL(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		//super(jsonObject.getLocation());
		super(jsonObject);
		try {
			this.url = new MustacheTemplate(jsonObject.getString(URL));
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**	
	 * Quick test to see whether a {@link JSONInterfaceObject} could deserialize into a {@link URL}.
	 * @param jsonObject The {@link JSONInterfaceObject} to test.
	 * @return <code>True</code> if the {@link JSONInterfaceObject} could contain
	 *  a URL, <code>false</code> otherwise.
	 */
	public static final boolean isURL(JSONInterfaceObject jsonObject) {
		if(jsonObject.has(URL)) {
			return true;
		}
		return false;
	}
}
