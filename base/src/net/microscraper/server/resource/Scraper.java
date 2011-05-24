package net.microscraper.server.resource;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.mixin.FindsMany;
import net.microscraper.server.resource.mixin.FindsOne;
import net.microscraper.server.resource.mixin.SpawnsScrapers;

/**
 * A scraper can include a {@link Page}, a set of {@link FindOne}s, a set of {@link FindMany}s, and a
 * set of other {@link Scraper}s to launch.
 * @author john
 *
 */
public class Scraper extends Resource implements FindsOne, FindsMany,
			SpawnsScrapers {
	private final FindsOne findsOne;
	private final FindsMany findsMany;
	private final SpawnsScrapers spawnsScrapers;
	
	public Scraper[] getScrapers() {
		return spawnsScrapers.getScrapers();
	}

	public FindMany[] getFindManys() {
		return findsMany.getFindManys();
	}

	public FindOne[] getFindOnes() {
		return findsOne.getFindOnes();
	}
	
	/**
		The optional source {@link Page} for this {@link Scraper}.
		Is <code>null</code> if {@link #hasSource} is <code>false</code>.
		@see #hasSource()
	*/
	public final Page sourcePage;
	
	/**
	 * Whether or not this {@link Scraper} has a source {@link Page}.
	 * @see #source
	 */
	public final boolean hasSource;
	
	private static final String SOURCE = "source";

	/**
	 * Deserialize a {@link Scraper} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Scraper} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Scraper}.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Scraper(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject.getLocation());
		try {
			this.findsMany = FindsMany.Deserializer.deserialize(jsonObject);
			this.findsOne = FindsOne.Deserializer.deserialize(jsonObject);
			this.spawnsScrapers = SpawnsScrapers.Deserializer.deserialize(jsonObject);
			
			if(jsonObject.has(SOURCE)) {
				hasSource = true;
				sourcePage = new Page(jsonObject.getJSONObject(SOURCE));
			} else {
				hasSource = false;
				sourcePage = null;
			}
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
