package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class MustacheNameValuePair {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
	public MustacheNameValuePair(MustacheTemplate name, MustacheTemplate value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Deserialize an array of {@link MustacheNameValuePair} from a hash in {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An array of {@link MustacheNameValuePair}.
	 * @throws DeserializationException If this is not a valid JSON Hash of MustacheableNameValuePairs.
	 */
	public static MustacheNameValuePair[] deserializeHash(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Object jsonObject) throws DeserializationException {
		Iterator iterator = jsonObject.keys();
		MustacheNameValuePair[] array = new MustacheNameValuePair[jsonObject.length()];
		int i = 0;
		while(iterator.hasNext()) {
			String name = (String) iterator.next();
			String value;
			try {
				value = jsonObject.getString(name);
			} catch (JSONInterfaceException e) {
				//throw new DeserializationException("Invalid element in MustacheNameValuePair hash at '" + name + "'");
				throw new DeserializationException(e, jsonObject);
			}
			array[i] = new MustacheNameValuePair(
						new MustacheTemplate(name),
						new MustacheTemplate(value)
					);
			i++;
		}
		return array;
	}
}
