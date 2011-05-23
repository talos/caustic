package net.microscraper.server.resource.mixin;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.Ref;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.FindMany;

/**
 * Implementations of this interface can produce {@link Executable}s that can spawn new {@link ScraperExecutableChild}s.
 * @author john
 *
 */
public interface SpawnsScrapers {
	
	/**
	 * 
	 * @return An array of {@link Ref}s to that can be used to connect to {@link ScraperExecutableChild}s.
	 */
	public abstract Ref[] getScrapers();
	

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link SpawnsScrapers} using an inner constructor.
	 * Should only be instantiated inside {@link FindMany} or {@link ScraperExecutable}.
	 * @see FindMany
	 * @see ScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		public static String KEY = "then";

		/**
		 * Deserialize an {@link SpawnsScrapers} from a {@link JSONInterfaceObject}.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these pipe references.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link SpawnsScrapers} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link SpawnsScrapers}.
		 */
		public static SpawnsScrapers deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final Ref[] pipes;
				if(jsonObject.has(KEY)) {
					pipes = Ref.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(KEY));				
				} else {
					pipes = new Ref[0];
				}
				return new SpawnsScrapers() {
					public Ref[] getScrapers() {
						return pipes;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
