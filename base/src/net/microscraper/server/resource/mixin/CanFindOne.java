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
public interface CanFindOne {
	
	/**
	 * 
	 * @return An array of associated {@link FindOne} {@link Resource}s.
	 */
	public abstract FindOne[] getFindOnes();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link CanFindOne} using an inner constructor.
	 * Should only be instantiated inside {@link FindOne} or {@link SpawnedScraperExecutable}.
	 * @see FindOne
	 * @see SpawnedScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String KEY = "finds_one";
		
		/**
		 * Protected, should be called only by {@link FindOne} or {@link SpawnedScraperExecutable}.
		 * Deserialize an {@link CanFindOne} from a {@link JSONInterfaceObject}.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link CanFindOne} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link CanFindOne}.
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static CanFindOne deserialize(JSONInterfaceObject jsonObject)
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
				return new CanFindOne() {
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
