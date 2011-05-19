package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

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
	 * Should only be instantiated inside {@link Variable} or {@link ScraperExecution}.
	 * @see Variable
	 * @see ScraperExecution
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String VARIABLES = "variables";
		
		/**
		 * Protected, should be called only by {@link Variable} or {@link ScraperExecution}.
		 * Deserialize an {@link HasVariables} from a {@link Interfaces.JSON.Object}.
		 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these variables.
		 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
		 * @return An {@link HasVariables} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasVariables}.
		 */
		protected static HasVariables deserialize(Interfaces.JSON jsonInterface,
						URI location, Interfaces.JSON.Object jsonObject)
					throws DeserializationException {
			try {
				final Variable[] variables = Variable.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(VARIABLES));				
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
