package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.instruction.mixin.CanFindMany;
import net.microscraper.instruction.mixin.CanFindOne;
import net.microscraper.instruction.mixin.CanSpawnScrapers;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;

/**
 * A scraper can include a {@link Page}, a set of {@link FindOne}s, a set of {@link FindMany}s, and a
 * set of other {@link Scraper}s to launch.
 * @author john
 *
 */
public class Scraper extends Instruction implements CanFindOne, CanFindMany,
			CanSpawnScrapers {
	/*private final CanFindOne findsOne;
	private final CanFindMany findsMany;
	private final CanSpawnScrapers spawnsScrapers;*/
	
	private final Scraper[] spawnScrapers;
	public Scraper[] getScrapers() throws DeserializationException, IOException {
		return spawnScrapers;
	}

	private final FindMany[] findManys;
	public FindMany[] getFindManys() {
		return findManys;
	}

	private final FindOne[] findOnes;
	public FindOne[] getFindOnes() {
		return findOnes;
	}

	private final Page[] spawnPages;
	public Page[] getPages() throws DeserializationException, IOException {
		return spawnPages;
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
		CanFindMany canFindMany = CanFindMany.Deserializer.deserialize(jsonObject);
		CanFindOne  canFindOne = CanFindOne.Deserializer.deserialize(jsonObject);
		CanSpawnScrapers canSpawnScrapers = CanSpawnScrapers.Deserializer.deserialize(jsonObject);
		
		this.spawnPages = canSpawnScrapers.getPages();
		this.spawnScrapers = canSpawnScrapers.getScrapers();
		this.findManys = canFindMany.getFindManys();
		this.findOnes  = canFindOne.getFindOnes();
	}
	
	public Scraper(String location, Page[] spawnPages, Scraper[] spawnScrapers,
			FindMany[] findManys, FindOne[] findOnes) {
		super(location);
		this.spawnPages = spawnPages;
		this.spawnScrapers = spawnScrapers;
		this.findManys = findManys;
		this.findOnes = findOnes;
	}
}
