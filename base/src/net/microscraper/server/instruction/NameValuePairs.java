package net.microscraper.server.instruction;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceIterator;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheNameValuePair;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Instruction;

public class NameValuePairs extends Instruction {
	public final MustacheNameValuePair[] pairs;
	
	/**
	 * Deserialize a {@link NameValuePairs} from a {@link JSONInterfaceObject} hash.
	 * @param jsonObject Input {@link JSONInterfaceObject} hash.
	 * @return A {@link NameValuePairs} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link NameValuePairs}.
	 */
	public NameValuePairs(JSONInterfaceObject jsonObject) throws DeserializationException {
		super(jsonObject.getLocation());
		try {
			this.pairs = new MustacheNameValuePair[jsonObject.length()];
			JSONInterfaceIterator iter = jsonObject.keys();
			int i = 0;
			while(iter.hasNext()) {
				String key = (String) iter.next();
				String value = jsonObject.getString(key);
				this.pairs[i] = new MustacheNameValuePair(
						new MustacheTemplate(key),
						new MustacheTemplate(value));
				i++;
			}
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
