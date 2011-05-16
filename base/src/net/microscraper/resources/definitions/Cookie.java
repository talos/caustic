package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces;

public final class Cookie {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
	public Cookie(MustacheNameValuePair nameValuePair) {
		name = nameValuePair.name;
		value = nameValuePair.value;
	}
	
	/**
	 * Deserialize an array of {@link Cookie} from a hash in {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An array of {@link Cookie}.
	 * @throws DeserializationException If this is not a valid JSON Hash of cookies.
	 */
	public static Cookie[] deserializeHash(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Object jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.deserializeHash(jsonInterface, jsonObject);
		Cookie[] cookies = new Cookie[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			cookies[i] = new Cookie(nameValuePairs[i]);
		}
		return cookies;
	}
}