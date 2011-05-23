package net.microscraper.server.resource.mixin;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.FindOne;

/**
 * Allows connections to an {@link FindOne} executable.
 * @author john
 *
 */
public interface FindsOne {
	
	/**
	 * 
	 * @return An array of associated {@link FindOne} executables.
	 */
	public abstract FindOne[] getVariables();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link FindsOne} using an inner constructor.
	 * Should only be instantiated inside {@link FindOne} or {@link ScraperExecutable}.
	 * @see FindOne
	 * @see ScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String KEY = "finds_one";
		
		/**
		 * Protected, should be called only by {@link FindOne} or {@link ScraperExecutable}.
		 * Deserialize an {@link FindsOne} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these variables.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link FindsOne} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link FindsOne}.
		 */
		public static FindsOne deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final FindOne[] variables;
				if(jsonObject.has(KEY)) {
					variables = FindOne.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(KEY));				
				} else {
					variables = new FindOne[0];
				}
				return new FindsOne() {
					public FindOne[] getVariables() {
						return variables;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
