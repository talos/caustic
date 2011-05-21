package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * This should be implemented by a class that produces {@link Execution}s
 * that can have {@link LeafExecution} children.
 * @see 
 * @author john
 *
 */
public interface HasLeaves {
	
	/**
	 * 
	 * @return An array of associated {@link Leaf} executables.
	 */
	public abstract Leaf[] getLeaves();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link HasLeaves} using an inner constructor.
	 * Should only be instantiated inside {@link Variable} or {@link ScraperExecution}.
	 * @see Variable
	 * @see ScraperExecution
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String LEAVES = "leaves";
		
		/**
		 * Protected, should be called only by {@link Variable} or {@link ScraperExecution}.
		 * Deserialize an {@link HasLeaves} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these leaves' links.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link HasLeaves} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasLeaves}.
		 */
		protected static HasLeaves deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final Leaf[] leaves;
				if(jsonObject.has(LEAVES)) {
					leaves = Leaf.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(LEAVES));
				} else {
					leaves = new Leaf[0];
				}
				return new HasLeaves() {
					public Leaf[] getLeaves() {
						return leaves;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
