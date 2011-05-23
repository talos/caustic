package net.microscraper.server.resource;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.Ref;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.mixin.FindsMany;
import net.microscraper.server.resource.mixin.FindsOne;
import net.microscraper.server.resource.mixin.SpawnsScrapers;

public class Scraper extends Resource implements FindsOne, FindsMany,
			SpawnsScrapers {
	private final FindsOne hasVariables;
	private final FindsMany hasLeaves;
	private final SpawnsScrapers hasPipes;
	
	//public final ScraperSource scraperSource;
	public Scraper(URI location, ScraperSource scraperSource, FindsOne hasVariables, FindsMany hasLeaves,
			SpawnsScrapers hasPipes) throws URIMustBeAbsoluteException {
		super(location);
		this.scraperSource = scraperSource;
		this.hasPipes = hasPipes;
		this.hasLeaves = hasLeaves;
		this.hasVariables = hasVariables;
	}

	public Ref[] getScrapers() {
		return hasPipes.getScrapers();
	}

	public FindMany[] getLeaves() {
		return hasLeaves.getLeaves();
	}

	public FindOne[] getVariables() {
		return hasVariables.getVariables();
	}
	
	public static final String SOURCE = "source";
	
	/**
	 * Deserialize a {@link Scraper} from a {@link JSONInterfaceObject}.
	 * @param location The scraper's {@link URI} location.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link Scraper} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a {@link Scraper}.
	 */
	public static Scraper deserialize(JSONInterface jsonInterface,
					URI location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			FindsMany hasLeaves = FindsMany.Deserializer.deserialize(jsonInterface, location, jsonObject);
			FindsOne hasVariables = FindsOne.Deserializer.deserialize(jsonInterface, location, jsonObject);
			SpawnsScrapers hasPipes = SpawnsScrapers.Deserializer.deserialize(jsonInterface, location, jsonObject);
			/*ScraperSource scraperSource;
			try {
				scraperSource = ScraperSource.deserialize(jsonInterface, location, jsonObject.getJSONObject(SOURCE));
			} catch(JSONInterfaceException e) {
				scraperSource = ScraperSource.fromString(jsonObject.getString(SOURCE));
			}*/
			return new Scraper(location, scraperSource, hasVariables, hasLeaves, hasPipes);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
