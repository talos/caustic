package net.microscraper.server.resource.mixin;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
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
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static FindsOne deserialize(JSONInterfaceObject jsonObject)
					throws DeserializationException, IOException {
			try {
				final FindOne[] findOnes;
				if(jsonObject.has(KEY)) {
					JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
					findOnes = new FindOne[array.length()];
					for(int i = 0 ; i < findOnes.length ; i ++) {
						findOnes[i] = new FindOne(array.getJSONObject(i));
					}
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
