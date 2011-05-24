package net.microscraper.server.resource.mixin;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.FindOne;

/**
 * Allows connections to a {@link FindOne} {@link Resource}.
 * @author john
 *
 */
public interface FindsOne {
	
	/**
	 * 
	 * @return An array of associated {@link FindOne} {@link Resource}s.
	 */
	public abstract FindOne[] getFindOnes();
	
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
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link FindsOne} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link FindsOne}.
		 */
		public static FindsOne deserialize(JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final FindOne[] findOnes;
				if(jsonObject.has(KEY)) {
					findOnes = (FindOne[]) FindOne.deserializeArray(jsonObject.getJSONArray(KEY));				
				} else {
					findOnes = new FindOne[0];
				}
				return new FindsOne() {
					public FindOne[] getFindOnes() {
						return findOnes;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
