package net.microscraper.server;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URIInterfaceSyntaxException;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.URIMustBeAbsoluteException;

public class Ref {
	
	/**
	 * The absolute location of the resource this link refers to.
	 */
	public final URIInterface location;
	public Ref (URIInterface root, String link) throws URIMustBeAbsoluteException {
		this.location = root.resolve(link);
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	public Ref (URIInterface absoluteURI) throws URIMustBeAbsoluteException {
		this.location = absoluteURI;
		if(this.location.isAbsolute() == false) {
			throw new URIMustBeAbsoluteException(this.location);
		}
	}
	

	/**
	 * The key of a {@link Ref} when deserializing.
	 */
	private static final String REF = "$ref";
	
	/**
	 * Deserialize a JSON Object into a {@link Ref} instance.
	 * @param rootURI The root {@link URIInterface} that this link resolves against.
	 * @param jsonObject A {@link JSONInterfaceObject} of one link.
	 * @return A {@link Ref} instance.
	 * @throws DeserializationException If there is an error in the JSON or the link is
	 * not a valid absolute URI.
	 */
	public static Ref deserialize(URIInterface rootURI, JSONInterfaceObject jsonObject)
		throws DeserializationException {
		try {
			try {
				return new Ref(rootURI, jsonObject.getString(KEY));
			} catch(URIInterfaceSyntaxException e) {
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
	 * @param rootURI The root {@link URI} that this link resolves against.
	 * @param jsonArray A {@link JSONInterfaceArray} of links.
	 * @return An array of {@link Links}s instances.
	 * @throws DeserializationException If there is an error in the JSON, or one of the links is
	 * not a valid absolute URI.
	 */
	public static Ref[] deserializeArray(URIInterface rootURI, JSONInterfaceArray jsonArray)
		throws DeserializationException {
		Ref[] links = new Ref[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length(); i ++) {
			try {
				links[i] = Ref.deserialize(rootURI, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return links;
	}
}
