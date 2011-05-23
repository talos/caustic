package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * A generic header to add to a Page request.
 * @author john
 *
 */
public final class Header extends MustacheUnencodedNameValuePair {
	/**
	 * The resource's identifier when deserializing.
	 */
	public static final String KEY = "header";
	
	/**
	 * The resource's identifier when deserializing many from a hash.
	 */
	public static final String PLURAL_KEY = "header";
	
	private final MustacheNameValuePair nameValuePair;
	public Header(MustacheNameValuePair nameValuePair) {
		this.nameValuePair = nameValuePair;
	}

	public MustacheTemplate getName() {
		return nameValuePair.getName();
	}

	public MustacheTemplate getValue() {
		return nameValuePair.getValue();
	}
	
	/**
	 * Deserialize an array of {@link Header} from a hash in {@link JSONInterfaceObject}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An array of {@link Header}.
	 * @throws DeserializationException If this is not a valid JSON Hash of headers.
	 */
	public static Header[] deserializeHash(JSONInterface jsonInterface,
			JSONInterfaceObject jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.Deserializer.deserializeHash(jsonInterface, jsonObject);
		Header[] headers = new Header[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			headers[i] = new Header(nameValuePairs[i]);
		}
		return headers;
	}
}