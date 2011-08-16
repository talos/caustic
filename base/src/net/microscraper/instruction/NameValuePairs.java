package net.microscraper.instruction;

import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONIterator;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;

public class NameValuePairs {
	/**
	 * Deserialize a {@link NameValuePairs} from a {@link JSONObjectInterface} hash.
	 * @param jsonObject Input {@link JSONObjectInterface} hash.
	 * @return A {@link NameValuePairs} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link NameValuePairs}.
	 */
	public static MustacheNameValuePair[] deserialize(JSONObjectInterface jsonObject) throws DeserializationException {
		try {
			MustacheNameValuePair[] pairs = new MustacheNameValuePair[jsonObject.length()];
			JSONIterator iter = jsonObject.keys();
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
		} catch(JSONParserException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheCompilationException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
