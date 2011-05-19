package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class Scraper extends Resource implements HasVariables, HasLeaves,
			HasPipes {
	private final HasVariables hasVariables;
	private final HasLeaves hasLeaves;
	private final HasPipes hasPipes;
	
	public final ScraperSource scraperSource;
	public Scraper(URI location, ScraperSource scraperSource, HasVariables hasVariables, HasLeaves hasLeaves,
			HasPipes hasPipes) throws URIMustBeAbsoluteException {
		super(location);
		this.scraperSource = scraperSource;
		this.hasPipes = hasPipes;
		this.hasLeaves = hasLeaves;
		this.hasVariables = hasVariables;
	}

	public Link[] getPipes() {
		return hasPipes.getPipes();
	}

	public Leaf[] getLeaves() {
		return hasLeaves.getLeaves();
	}

	public Variable[] getVariables() {
		return hasVariables.getVariables();
	}
	
	public static final String SOURCE = "source";
	
	/**
	 * Deserialize a {@link ScraperExecution} from a {@link Interfaces.JSON.Object}.
	 * @param location The scraper's {@link URI} location.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An {@link Variable} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a ContextRoot.
	 */
	public static Scraper deserialize(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			HasLeaves hasLeaves = HasLeaves.Deserializer.deserialize(jsonInterface, location, jsonObject);
			HasVariables hasVariables = HasVariables.Deserializer.deserialize(jsonInterface, location, jsonObject);
			HasPipes hasPipes = HasPipes.Deserializer.deserialize(jsonInterface, location, jsonObject);
			ScraperSource scraperSource;
			try {
				scraperSource = ScraperSource.fromString(jsonObject.getString(SOURCE));
			} catch(JSONInterfaceException e) {
				scraperSource = ScraperSource.deserialize(jsonInterface, location, jsonObject.getJSONObject(SOURCE));
			}
			return new Scraper(location, scraperSource, hasVariables, hasLeaves, hasPipes);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
