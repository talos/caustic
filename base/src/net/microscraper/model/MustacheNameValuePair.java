package net.microscraper.model;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceIterator;
import net.microscraper.client.interfaces.JSONInterfaceObject;

public interface MustacheNameValuePair {
	public abstract MustacheTemplate getName();
	public abstract MustacheTemplate getValue();

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link MustacheNameValuePair} using an inner constructor.
	 * Should only be instantiated inside {@link Cookie}, or {@link Header}, or
	 * {@link Post}.
	 * @see Post
	 * @see Header
	 * @see Cookie
	 * @author john
	 *
	 */
	public static class Deserializer {		

		/**
		 * Deserialize an array of {@link MustacheNameValuePair} from a hash in {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An array of {@link MustacheNameValuePair}.
		 * @throws DeserializationException If this is not a valid JSON Hash of MustacheableNameValuePairs.
		 */
		public static MustacheNameValuePair[] deserializeHash(JSONInterface jsonInterface,
				JSONInterfaceObject jsonObject) throws DeserializationException {
			JSONInterfaceIterator iterator = jsonObject.keys();
			MustacheNameValuePair[] array = new MustacheNameValuePair[jsonObject.length()];
			int i = 0;
			while(iterator.hasNext()) {
				try {
					String name = (String) iterator.next();
					final MustacheTemplate mustacheValue = new MustacheTemplate(jsonObject.getString(name));
					final MustacheTemplate mustacheName = new MustacheTemplate(name);
					array[i] = new MustacheNameValuePair() {
						public MustacheTemplate getName() {
							return mustacheName;
						}
						public MustacheTemplate getValue() {
							return mustacheValue;
						}
					};
					i++;
				} catch (JSONInterfaceException e) {
					//throw new DeserializationException("Invalid element in MustacheNameValuePair hash at '" + name + "'");
					throw new DeserializationException(e, jsonObject);
				}
			}
			return array;
		}
	}
}
