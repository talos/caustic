package net.microscraper.instruction.mixin;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;

/**
 * Implementations of this interface can link to {@link Scraper} {@link Instruction}s.
 * @author john
 *
 */
public interface CanSpawnPages {
	
	/**
	 * 
	 * @return An array of {@link Page}s.
	 * @throws IOException 
	 * @throws DeserializationException 
	 */
	public abstract Page[] getPages() throws DeserializationException, IOException;
	

	/**
	 * A helper class to deserialize 
	 * interfaces of {@link CanSpawnPages} using an inner constructor.
	 * Should only be instantiated inside {@link FindMany} or {@link SpawnedScraperExecutable}.
	 * @see FindMany
	 * @see SpawnedScraperExecutable
	 * @author john
	 *
	 */
	public static class Deserializer {
		public static String KEY = "then";

		/**
		 * Deserialize a {@link CanSpawnPages} from a {@link JSONInterfaceObject}.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return A {@link CanSpawnPages} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link CanSpawnPages}.
		 * @throws IOException If there is an error loading one of the references.
		 */
		public static CanSpawnPages deserialize(final JSONInterfaceObject jsonObject)
					throws DeserializationException, IOException {
			return new CanSpawnPages() {
				private Page[] pages;
				
				private void load() throws JSONInterfaceException, IOException, DeserializationException {
					if(jsonObject.has(KEY)) {
						
						// If the key refers directly to an object, it is considered
						// an array of 1.
						if(jsonObject.isJSONObject(KEY)) {
							JSONInterfaceObject obj = jsonObject.getJSONObject(KEY);
							this.pages = new Page[] { new Page(obj) };
						} else {
							final JSONInterfaceArray array = jsonObject.getJSONArray(KEY);
							
							Vector pages = new Vector();
							
							for(int i = 0 ; i < array.length() ; i ++) {
								//scrapers[i] = new Scraper(array.getJSONObject(i));
								JSONInterfaceObject obj = array.getJSONObject(i);
								pages.add(new Page(obj));
							}
							this.pages = new Page[pages.size()];
							pages.copyInto(this.pages);
						}						

					} else {
						this.pages    = new Page[] {};
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
