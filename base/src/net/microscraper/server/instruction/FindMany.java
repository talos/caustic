package net.microscraper.server.instruction;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.mixin.CanSpawnScrapers;

/**
 * A {@link Find} that can connect to other {@link Scraper} through {@link #getPipes},
 * and is one-to-many (even if it only has one result.)
 * Its executions do not implement {@link Variables}, because {@link FindMany} can be one-to-many.
 * @see Find
 * @see CanSpawnScrapers
 * @author john
 *
 */
public class FindMany extends Find implements CanSpawnScrapers {	
	private final CanSpawnScrapers spawnsScrapers;
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * Defaults to {@link #DEFAULT_MIN_MATCH}.
	 * @see #maxMatch
	 * @see FindOne#match
	 */
	public final int minMatch;
	
	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * @see #minMatch
	 * @see FindOne#match
	 */
	public final int maxMatch;
	
	/**
	 * Deserialize a {@link FindMany} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link FindMany} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link FindMany},
	 * or the location is invalid.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public FindMany(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			this.minMatch = jsonObject.has(MIN_MATCH) ? jsonObject.getInt(MIN_MATCH) : DEFAULT_MIN_MATCH;
			this.maxMatch = jsonObject.has(MAX_MATCH) ? jsonObject.getInt(MAX_MATCH) : DEFAULT_MAX_MATCH;
			this.spawnsScrapers = CanSpawnScrapers.Deserializer.deserialize(jsonObject);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public Scraper[] getScrapers() throws DeserializationException, IOException {
		return spawnsScrapers.getScrapers();
	}
	
	public Page[] getPages() throws DeserializationException, IOException {
		return spawnsScrapers.getPages();
	}
	
	private static final String MIN_MATCH = "min";
	private static final String MAX_MATCH = "max";
	
	/**
	 * The first of any number of matches.
	 */
	private static final int DEFAULT_MIN_MATCH = 0;
	
	/**
	 * The last of any number of matches.
	 */
	private static final int DEFAULT_MAX_MATCH = -1;
}
