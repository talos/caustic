package net.microscraper.server.resource;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.mixin.CanFindMany;
import net.microscraper.server.resource.mixin.CanFindOne;
import net.microscraper.server.resource.mixin.CanSpawnScrapers;

/**
 * A scraper can include a {@link Page}, a set of {@link FindOne}s, a set of {@link FindMany}s, and a
 * set of other {@link Scraper}s to launch.
 * @author john
 *
 */
public class Scraper extends Resource implements CanFindOne, CanFindMany,
			CanSpawnScrapers {
	private final CanFindOne findsOne;
	private final CanFindMany findsMany;
	private final CanSpawnScrapers spawnsScrapers;
	
	public Scraper[] getScrapers() throws DeserializationException, IOException {
		return spawnsScrapers.getScrapers();
	}

	public FindMany[] getFindManys() {
		return findsMany.getFindManys();
	}

	public FindOne[] getFindOnes() {
		return findsOne.getFindOnes();
	}

	public Page[] getPages() throws DeserializationException, IOException {
		return spawnsScrapers.getPages();
	}
	
	/**
	 * Deserialize a {@link Scraper} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Scraper} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Scraper}.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Scraper(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject.getLocation());
		this.findsMany = CanFindMany.Deserializer.deserialize(jsonObject);
		this.findsOne = CanFindOne.Deserializer.deserialize(jsonObject);
		this.spawnsScrapers = CanSpawnScrapers.Deserializer.deserialize(jsonObject);
	}
}
