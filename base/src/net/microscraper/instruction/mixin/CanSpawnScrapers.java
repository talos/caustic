package net.microscraper.instruction.mixin;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Scraper;
import net.microscraper.instruction.URL;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;

/**
 * Implementations of this interface can link to {@link Scraper} {@link Instruction}s.
 * @author john
 *
 */
public interface CanSpawnScrapers {
	
	/**
	 * 
	 * @return An array of {@link Scraper}s.
	 * @throws IOException 
	 * @throws DeserializationException 
	 */
	public abstract Scraper[] getScrapers() throws DeserializationException, IOException;
	
	/**
	 * 
	 * @return An array of {@link Page}s.
	 * @throws IOException 
	 * @throws DeserializationException 
	 */
	public abstract Page[] getPages() throws DeserializationException, IOException;
	

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link CanSpawnScrapers} using an inner constructor.
	 * Should only be instantiated inside {@link FindMany} or {@link SpawnedScraperExecutable}.
	 * @see FindMany
	 * @see SpawnedScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		public static String KEY = "then";

		/**
		 * Deserialize a {@link CanSpawnScrapers} from a {@link JSONInterfaceObject}.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return A {@link CanSpawnScrapers} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link CanSpawnScrapers}.
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static CanSpawnScrapers deserialize(final JSONInterfaceObject jsonObject)
					throws DeserializationException, IOException {
			return new CanSpawnScrapers() {
				private Scraper[] scrapers;
				private Page[] pages;
				
				private void load() throws JSONInterfaceException, IOException, DeserializationException {
					if(jsonObject.has(KEY)) {
						final JSONInterfaceArray array = jsonObject.getJSONArray(KEY);

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
						this.scrapers = new Scraper[scrapers.size()];
						this.pages = new Page[pages.size()];
						scrapers.copyInto(this.scrapers);
						pages.copyInto(this.pages);

					} else {
						this.scrapers = new Scraper[0];
						this.pages = new Page[0];
					}
					
				}
				public Scraper[] getScrapers() throws DeserializationException, IOException {
					try {
						if(scrapers == null)
							load();
						return scrapers;
					} catch(JSONInterfaceException e) {
						throw new DeserializationException(e, jsonObject);
					}
				}
				public Page[] getPages() throws DeserializationException, IOException {
					try {
						if(pages == null)
							load();
						return pages;
					} catch(JSONInterfaceException e) {
						throw new DeserializationException(e, jsonObject);
					}
				}
			};
		}
	}
}
