package net.microscraper.model;

import net.microscraper.client.Interfaces;

/**
 * A generic header to add to a Page request.
 * @author john
 *
 */
public final class Header extends MustacheUnencodedNameValuePair {
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
	 * Deserialize an array of {@link Header} from a hash in {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An array of {@link Header}.
	 * @throws DeserializationException If this is not a valid JSON Hash of headers.
	 */
	public static Header[] deserializeHash(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Object jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.Deserializer.deserializeHash(jsonInterface, jsonObject);
		Header[] headers = new Header[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			headers[i] = new Header(nameValuePairs[i]);
		}
		return headers;
	}
}