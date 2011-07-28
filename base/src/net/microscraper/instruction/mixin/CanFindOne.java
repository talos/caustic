package net.microscraper.instruction.mixin;

import java.io.IOException;

import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;

/**
 * Allows connections to a {@link FindOne} {@link Instruction}.
 * @author john
 *
 */
public interface CanFindOne {
	
	/**
	 * 
	 * @return An array of associated {@link FindOne} {@link Instruction}s.
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
					// If the key refers directly to an object, it is considered
					// an array of 1.
					if(jsonObject.isJSONObject(KEY)) {
						findOnes = new FindOne[] {
								new FindOne(jsonObject.getJSONObject(KEY))
						};
					} else {
						JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
						findOnes = new FindOne[array.length()];
						for(int i = 0 ; i < findOnes.length ; i ++) {
							findOnes[i] = new FindOne(array.getJSONObject(i));
						}
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
