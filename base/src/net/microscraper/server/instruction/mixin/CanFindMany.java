package net.microscraper.server.instruction.mixin;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.FindMany;
import net.microscraper.server.instruction.FindOne;

/**
 * Allows connections to a {@link FindMany} {@link Instruction}.
 * @author john
 *
 */
public interface CanFindMany {
	
	/**
	 * 
	 * @return An array of associated {@link FindMany} {@link Instruction}s.
	 */
	public abstract FindMany[] getFindManys();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link CanFindMany} using an inner constructor.
	 * Should only be instantiated inside {@link FindOne} or {@link SpawnedScraperExecutable}.
	 * @see FindOne
	 * @see SpawnedScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String KEY = "finds_many";
		
		/**
		 * Deserialize an {@link CanFindMany} from a {@link JSONInterfaceObject}.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link CanFindMany} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link CanFindMany}.
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static CanFindMany deserialize(JSONInterfaceObject jsonObject)
					throws DeserializationException, IOException {
			try {
				final FindMany[] findManys;
				if(jsonObject.has(KEY)) {
					JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
					findManys = new FindMany[array.length()];
					for(int i = 0 ; i < findManys.length ; i ++) {
						findManys[i] = new FindMany(array.getJSONObject(i));
					}
				} else {
					findManys = new FindMany[0];
				}
				return new CanFindMany() {
					public FindMany[] getFindManys() {
						return findManys;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
