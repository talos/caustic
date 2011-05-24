package net.microscraper.server.resource.mixin;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Scraper;

/**
 * Implementations of this interface can link to {@link Scraper} {@link Resource}s.
 * @author john
 *
 */
public interface SpawnsScrapers {
	
	/**
	 * 
	 * @return An array of {@link Scraper}s.
	 */
	public abstract Scraper[] getScrapers();
	

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
		 * Deserialize a {@link SpawnsScrapers} from a {@link JSONInterfaceObject}.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return A {@link SpawnsScrapers} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link SpawnsScrapers}.
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static SpawnsScrapers deserialize(JSONInterfaceObject jsonObject)
					throws DeserializationException, IOException {
			try {
				final Scraper[] scrapers;
				if(jsonObject.has(KEY)) {
					JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
					scrapers = new Scraper[array.length()];
					for(int i = 0 ; i < scrapers.length ; i ++) {
						scrapers[i] = new Scraper(array.getJSONObject(i));
					}
				} else {
					scrapers = new Scraper[0];
				}
				return new SpawnsScrapers() {
					public Scraper[] getScrapers() {
						return scrapers;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
