package net.microscraper.model;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class Link {
	/**
	 * The absolute location of the resource this link refers to.
	 */
	public final URI location;
	public Link (URI root, URI link) throws URIMustBeAbsoluteException {
		this.location = root.resolve(link);
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	public Link (URI absoluteURI) throws URIMustBeAbsoluteException {
		this.location = absoluteURI;
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	
	private static final String REF = "$ref";
	
	/**
	 * Deserialize a JSON Object into a {@link Link} instance.
	 * @param jsonInterface The {@link Interfaces.JSON} to use in processing JSON.
	 * @param rootURI The root {@link URI} that this link resolves against.
	 * @param jsonObject A {@link Interfaces.JSON.Object} of one link.
	 * @return A {@link Link} instance.
	 * @throws DeserializationException If there is an error in the JSON or the link is
	 * not a valid absolute URI.
	 */
	public static Link deserialize(Interfaces.JSON jsonInterface,
			URI rootURI, Interfaces.JSON.Object jsonObject)
		throws DeserializationException {
		try {
			try {
				return new Link(rootURI, new URI(jsonObject.getString(REF)));
			} catch(URISyntaxException e) {
				throw new DeserializationException("Link '" + jsonObject.getString(REF) + "' is not a URI.", jsonObject);
			} catch(URIMustBeAbsoluteException e) {
				throw new DeserializationException("Link '" + jsonObject.getString(REF) + "' is not resolving to be absolute.", jsonObject);				
			}
		} catch (JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	
	/**
	 * Deserialize a JSON Array into an array of {@link Link} instances.
	 * @param jsonInterface The {@link Interfaces.JSON} to use in processing JSON.
	 * @param rootURI The root {@link URI} that this link resolves against.
	 * @param jsonArray A {@link Interfaces.JSON.Array} of links.
	 * @return An array of {@link Links}s instances.
	 * @throws DeserializationException If there is an error in the JSON, or one of the links is
	 * not a valid absolute URI.
	 */
	public static Link[] deserializeArray(Interfaces.JSON jsonInterface,
			URI rootURI, Interfaces.JSON.Array jsonArray)
		throws DeserializationException {
		Link[] links = new Link[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length(); i ++) {
			try {
				links[i] = Link.deserialize(jsonInterface, rootURI, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return links;
	}
}
