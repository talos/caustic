package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.Ref;
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
	
	//public final ScraperSource scraperSource;
	public Scraper(URIInterface location, FindsOne hasVariables, FindsMany hasLeaves,
			SpawnsScrapers hasPipes) throws URIMustBeAbsoluteException {
		super(location);
		this.spawnsScrapers = hasPipes;
		this.findsMany = hasLeaves;
		this.findsOne = hasVariables;
	}
	public Scraper(URIInterface location, Page source, FindsOne hasVariables, FindsMany hasLeaves,
			SpawnsScrapers hasPipes) throws URIMustBeAbsoluteException {
		super(location);
		this.spawnsScrapers = hasPipes;
		this.findsMany = hasLeaves;
		this.findsOne = hasVariables;
	}

	public Scraper[] getScrapers() {
		return spawnsScrapers.getScrapers();
	}

	public FindMany[] getFindManys() {
		return findsMany.getFindManys();
	}

	public FindOne[] getFindOnes() {
		return findsOne.getFindOnes();
	}
	
	private static final String SOURCE = "source";
	
	/**
	 * Deserialize a {@link Scraper} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link Scraper} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a {@link Scraper}.
	 */
	public static Scraper deserialize(JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			FindsMany hasLeaves = FindsMany.Deserializer.deserialize(jsonObject);
			FindsOne hasVariables = FindsOne.Deserializer.deserialize(jsonObject);
			SpawnsScrapers hasPipes = SpawnsScrapers.Deserializer.deserialize(jsonObject);
			
			if(jsonObject.has(SOURCE)) {
				Page page = Page.deserialize(jsonObject.getJSONObject(SOURCE));
				return new Scraper(jsonObject.getLocation(), hasVariables, hasLeaves, hasPipes);
			} else {
				return new Scraper(jsonObject.getLocation(), hasVariables, hasLeaves, hasPipes);
			}
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	public static Scraper[] deserializeArray(JSONInterfaceArray jsonArray) {
		// TODO Auto-generated method stub
		return null;
	}
}
