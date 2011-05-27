package net.microscraper.server.resource.mixin;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Scraper;
import net.microscraper.server.resource.URL;

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
	 * 
	 * @return An array of {@link Page}s.
	 */
	public abstract Page[] getPages();
	

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link SpawnsScrapers} using an inner constructor.
	 * Should only be instantiated inside {@link FindMany} or {@link SpawnedScraperExecutable}.
	 * @see FindMany
	 * @see SpawnedScraperExecutable
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
				final Scraper[] scrapersAry;
				final Page[] pagesAry;
				if(jsonObject.has(KEY)) {
					JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
					
					Vector pages = new Vector();
					Vector scrapers = new Vector();
					
					//scrapers = new Scraper[array.length()];
					for(int i = 0 ; i < array.length() ; i ++) {
						//scrapers[i] = new Scraper(array.getJSONObject(i));
						JSONInterfaceObject obj = array.getJSONObject(i);
						if(URL.isURL(obj) == true) {
							pages.add(new Page(obj));
						} else {
							scrapers.add(new Scraper(obj));
						}
					}
					
					scrapersAry = new Scraper[scrapers.size()];
					pagesAry = new Page[pages.size()];
					scrapers.copyInto(scrapersAry);
					pages.copyInto(pagesAry);
				} else {
					scrapersAry = new Scraper[0];
					pagesAry = new Page[0];
				}
				return new SpawnsScrapers() {
					public Scraper[] getScrapers() {
						return scrapersAry;
					}
					public Page[] getPages() {
						return pagesAry;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
