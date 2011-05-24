package net.microscraper.server.resource;

import java.io.IOException;
import java.net.MalformedURLException;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Resource;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL extends Resource {	
	public final MustacheTemplate template;
	public URL(URIInterface location, MustacheTemplate template) throws URIMustBeAbsoluteException {
		super(location);
		this.template = template;
	}
	
	/**
	 * Create a {@link URL} from a String.
	 * @param String Input string.
	 * @return A {@link URL} instance.
	 */
	/*public static URL fromString(String urlTemplate) {
		return new URL(new MustacheTemplate(urlTemplate));
	}*/
	
	/**
	 * 
	 */
	
	private static final String URL = "url";

	/**
	 * Deserialize a {@link URL} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link URL} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link URL}.
	 */
	public static URL deserialize(JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			return new URL(jsonObject.getLocation(), new MustacheTemplate(jsonObject.getString(URL)));
		} catch (URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		} catch (JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
