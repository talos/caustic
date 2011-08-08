package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheTemplate;
import net.microscraper.instruction.mixin.CanSpawnPages;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * A {@link Find} that can connect to other {@link Scraper} through {@link #getScrapers()},
 * and is one-to-many (even if it only has one result.)
 * Its executions do not implement {@link Variables}, because {@link FindMany} can be one-to-many.
 * @see Find
 * @see CanSpawnPages
 * @author john
 *
 */
public class FindMany extends Find {		
	private final int minMatch;

	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * Defaults to {@link #DEFAULT_MIN_MATCH}.
	 * @see #maxMatch
	 * @see FindOne#match
	 */
	public final int getMinMatch() {
		return minMatch;
	}
	
	private final int maxMatch;
	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * @see #minMatch
	 * @see FindOne#match
	 */
	public final int getMaxMatch() {
		return maxMatch;
	}
	
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
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public FindMany(JSONLocation location, MustacheTemplate name, FindOne[] findOnes,
			FindMany[] findManys, Page[] spawnPages,
			Regexp regexp, Regexp[] tests, MustacheTemplate replacement, int minMatch, int maxMatch) {
		super(location, name, findOnes, findManys,
				spawnPages, regexp, tests, replacement);
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
	}
	
	/**
	 * Key for {@link #getMinMatch()} value when deserializing from JSON.
	 */
	public static final String MIN_MATCH = "min";
	
	/**
	 * Key for {@link #getMaxMatch()} value when deserializing from JSON.
	 */
	public static final String MAX_MATCH = "max";
	
	/**
	 * {@link #getMinMatch()} defaults to the first of any number of matches.
	 */
	public static final int DEFAULT_MIN_MATCH = 0;
	
	/**
	 * {@link #getMaxMatch()} defaults to the last of any number of matches.
	 */
	public static final int DEFAULT_MAX_MATCH = -1;
}
