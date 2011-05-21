package net.microscraper.model;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceObject;

public final class Cookie extends MustacheEncodedNameValuePair {
	private final MustacheNameValuePair nameValuePair;
	public Cookie(MustacheNameValuePair nameValuePair) {
		this.nameValuePair = nameValuePair;
	}

	public MustacheTemplate getName() {
		return nameValuePair.getName();
	}

	public MustacheTemplate getValue() {
		return nameValuePair.getValue();
	}
	
	/**
	 * Deserialize an array of {@link Cookie} from a hash in {@link JSONInterfaceObject}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An array of {@link Cookie}.
	 * @throws DeserializationException If this is not a valid JSON Hash of cookies.
	 */
	public static Cookie[] deserializeHash(JSONInterface jsonInterface,
			JSONInterfaceObject jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.Deserializer.deserializeHash(jsonInterface, jsonObject);
		Cookie[] cookies = new Cookie[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			cookies[i] = new Cookie(nameValuePairs[i]);
		}
		return cookies;
	}
}