package net.microscraper.server.resource.mixin;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.FindOne;

/**
 * This should be implemented by a class that produces {@link Executable}s
 * that can have {@link LeafExecutable} children.
 * @see 
 * @author john
 *
 */
public interface FindsMany {
	
	/**
	 * 
	 * @return An array of associated {@link FindMany} executables.
	 */
	public abstract FindMany[] getLeaves();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link FindsMany} using an inner constructor.
	 * Should only be instantiated inside {@link FindOne} or {@link ScraperExecutable}.
	 * @see FindOne
	 * @see ScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String LEAVES = "leaves";
		
		/**
		 * Protected, should be called only by {@link FindOne} or {@link ScraperExecutable}.
		 * Deserialize an {@link FindsMany} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these leaves' links.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link FindsMany} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link FindsMany}.
		 */
		public static FindsMany deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final FindMany[] leaves;
				if(jsonObject.has(LEAVES)) {
					leaves = FindMany.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(LEAVES));
				} else {
					leaves = new FindMany[0];
				}
				return new FindsMany() {
					public FindMany[] getLeaves() {
						return leaves;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
