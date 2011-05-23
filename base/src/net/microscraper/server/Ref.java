package net.microscraper.server;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.URIMustBeAbsoluteException;

public class Ref {
	
	/**
	 * The key of a {@link Ref} when deserializing.
	 */
	public static final String KEY = "$ref";
	
	/**
	 * The absolute location of the resource this link refers to.
	 */
	public final URI location;
	public Ref (URI root, URI link) throws URIMustBeAbsoluteException {
		this.location = root.resolve(link);
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	public Ref (URI absoluteURI) throws URIMustBeAbsoluteException {
		this.location = absoluteURI;
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	
	
	/**
	 * Deserialize a JSON Object into a {@link Ref} instance.
	 * @param jsonInterface The {@link JSONInterface} to use in processing JSON.
	 * @param rootURI The root {@link URI} that this link resolves against.
	 * @param jsonObject A {@link JSONInterfaceObject} of one link.
	 * @return A {@link Ref} instance.
	 * @throws DeserializationException If there is an error in the JSON or the link is
	 * not a valid absolute URI.
	 */
	public static Ref deserialize(JSONInterface jsonInterface,
			URI rootURI, JSONInterfaceObject jsonObject)
		throws DeserializationException {
		try {
			try {
				return new Ref(rootURI, new URI(jsonObject.getString(KEY)));
			} catch(URISyntaxException e) {
				throw new DeserializationException("Link '" + jsonObject.getString(KEY) + "' is not a URI.", jsonObject);
			} catch(URIMustBeAbsoluteException e) {
				throw new DeserializationException("Link '" + jsonObject.getString(KEY) + "' is not resolving to be absolute.", jsonObject);				
			}
		} catch (JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	
	/**
	 * Deserialize a JSON Array into an array of {@link Ref} instances.
	 * @param jsonInterface The {@link JSONInterface} to use in processing JSON.
	 * @param rootURI The root {@link URI} that this link resolves against.
	 * @param jsonArray A {@link JSONInterfaceArray} of links.
	 * @return An array of {@link Links}s instances.
	 * @throws DeserializationException If there is an error in the JSON, or one of the links is
	 * not a valid absolute URI.
	 */
	public static Ref[] deserializeArray(JSONInterface jsonInterface,
			URI rootURI, JSONInterfaceArray jsonArray)
		throws DeserializationException {
		Ref[] links = new Ref[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length(); i ++) {
			try {
				links[i] = Ref.deserialize(jsonInterface, rootURI, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return links;
	}
}
