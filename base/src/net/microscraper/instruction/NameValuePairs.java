package net.microscraper.instruction;

import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplate;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceIterator;
import net.microscraper.interfaces.json.JSONInterfaceObject;

public class NameValuePairs {
	/**
	 * Deserialize a {@link NameValuePairs} from a {@link JSONInterfaceObject} hash.
	 * @param jsonObject Input {@link JSONInterfaceObject} hash.
	 * @return A {@link NameValuePairs} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link NameValuePairs}.
	 */
	public static MustacheNameValuePair[] deserialize(JSONInterfaceObject jsonObject) throws DeserializationException {
		try {
			MustacheNameValuePair[] pairs = new MustacheNameValuePair[jsonObject.length()];
			JSONInterfaceIterator iter = jsonObject.keys();
			int i = 0;
			while(iter.hasNext()) {
				String key = (String) iter.next();
				String value = jsonObject.getString(key);
				pairs[i] = new MustacheNameValuePair(
						new MustacheTemplate(key),
						new MustacheTemplate(value));
				i++;
			}
			return pairs;
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
