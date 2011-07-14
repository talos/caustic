package net.microscraper.server.instruction;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.mixin.CanFindMany;
import net.microscraper.server.instruction.mixin.CanFindOne;

/**
 * A one-to-one {@link Find} that can link to other {@link Find}s,
 * both {@link FindOne} and {@link FindMany}.  It cannot be piped, although
 * its value is exported when its parent scraper is piped.
 * @author john
 *
 */
public class FindOne extends Find implements CanFindMany, CanFindOne {
	
	/**
	 * The resource's identifier when deserializing.
	 */
	public static final String KEY = "find_one";
	
	private final CanFindMany findsMany;
	private final CanFindOne findsOne;
	
	/**
	 * A {@link FindOne} finds a single scraper match.
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * Defaults to {@link #DEFAULT_MATCH}.
	 * @see FindMany#minMatch
	 * @see FindMany#maxMatch
	 */
	public final int match;

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
			this.findsOne = CanFindOne.Deserializer.deserialize(jsonObject);
			this.findsMany = CanFindMany.Deserializer.deserialize(jsonObject);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public FindOne[] getFindOnes() {
		return findsOne.getFindOnes();
	}

	public FindMany[] getFindManys() {
		return findsMany.getFindManys();
	}
	
	private static final String MATCH = "match";
	
	/**
	 * The first match.
	 */
	private static final int DEFAULT_MATCH = 0;
}
