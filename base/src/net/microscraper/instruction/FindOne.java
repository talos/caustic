package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheTemplate;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.uri.URIInterface;

/**
 * A one-to-one {@link Find} that can link to other {@link Find}s,
 * both {@link FindOne} and {@link FindMany}.  It cannot be piped, although
 * its value is exported when its parent scraper is piped.
 * @author john
 *
 */
public class FindOne extends Find {
	
	/**
	 * The resource's identifier when deserializing.
	 */
	public static final String KEY = "find_one";
	
	private final int match;
	/**
	 * A {@link FindOne} finds a single scraper match. It is
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * Defaults to {@link #DEFAULT_MATCH}.
	 * @return This {@link FindOne}'s match number.
	 * @see FindMany#minMatch
	 * @see FindMany#maxMatch
	 */
	public int getMatch() {
		return match;
	}
	
	/**
	 * Deserialize a {@link FindOne} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link FindOne} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link FindOne}.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public FindOne(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			this.match = jsonObject.has(MATCH) ? jsonObject.getInt(MATCH) : DEFAULT_MATCH;
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	/**
	 * Key for {@link #getMatch()} value when deserializing from JSON.
	 */
	private static final String MATCH = "match";
	
	/**
	 * Defaults to <code>0</code>, the first match.
	 */
	private static final int DEFAULT_MATCH = 0;
}
