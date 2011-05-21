package net.microscraper.server.resource;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * Allows connections to an {@link Variable} executable.
 * @author john
 *
 */
public interface HasVariables {
	
	/**
	 * 
	 * @return An array of associated {@link Variable} executables.
	 */
	public abstract Variable[] getVariables();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link HasVariables} using an inner constructor.
	 * Should only be instantiated inside {@link Variable} or {@link ScraperExecutable}.
	 * @see Variable
	 * @see ScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String VARIABLES = "variables";
		
		/**
		 * Protected, should be called only by {@link Variable} or {@link ScraperExecutable}.
		 * Deserialize an {@link HasVariables} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these variables.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link HasVariables} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasVariables}.
		 */
		protected static HasVariables deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final Variable[] variables;
				if(jsonObject.has(VARIABLES)) {
					variables = Variable.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(VARIABLES));				
				} else {
					variables = new Variable[0];
				}
				return new HasVariables() {
					public Variable[] getVariables() {
						return variables;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
