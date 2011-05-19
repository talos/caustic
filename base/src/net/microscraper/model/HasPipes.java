package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

/**
 * Permits connection to another scraper through a reference.
 * @author john
 *
 */
public interface HasPipes {
	
	/**
	 * 
	 * @return An array of {@link Link}s to connected {@link ScraperExecution}s.
	 */
	public abstract Link[] getPipes();
	

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link HasPipes} using an inner constructor.
	 * Should only be instantiated inside {@link Leaf} or {@link ScraperExecution}.
	 * @see Leaf
	 * @see ScraperExecution
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String PIPES = "pipes";
		
		/**
		 * Protected, should be called only by {@link Leaf} or {@link ScraperExecution}.
		 * Deserialize an {@link HasPipes} from a {@link Interfaces.JSON.Object}.
		 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these pipe references.
		 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
		 * @return An {@link HasPipes} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasPipes}.
		 */
		protected static HasPipes deserialize(Interfaces.JSON jsonInterface,
						URI location, Interfaces.JSON.Object jsonObject)
					throws DeserializationException {
			try {
				final Link[] pipes = Link.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(PIPES));				
				return new HasPipes() {
					public Link[] getPipes() {
						return pipes;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
