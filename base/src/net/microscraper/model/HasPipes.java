package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * Implementations of this interface can produce {@link Execution}s that can spawn new {@link ScraperExecutionChild}s.
 * @author john
 *
 */
public interface HasPipes {
	
	/**
	 * 
	 * @return An array of {@link Link}s to that can be used to connect to {@link ScraperExecutionChild}s.
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
		 * Deserialize an {@link HasPipes} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these pipe references.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link HasPipes} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasPipes}.
		 */
		protected static HasPipes deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final Link[] pipes;
				if(jsonObject.has(PIPES)) {
					pipes = Link.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(PIPES));				
				} else {
					pipes = new Link[0];
				}
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
