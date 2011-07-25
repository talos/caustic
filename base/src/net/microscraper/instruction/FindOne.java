package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheTemplate;
import net.microscraper.instruction.mixin.CanFindMany;
import net.microscraper.instruction.mixin.CanFindOne;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

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
			CanFindOne canFindOne = CanFindOne.Deserializer.deserialize(jsonObject);
			CanFindMany canFindMany = CanFindMany.Deserializer.deserialize(jsonObject);
			this.findOnes = canFindOne.getFindOnes();
			this.findManys = canFindMany.getFindManys();
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public FindOne(JSONLocation location, MustacheTemplate pattern, boolean isCaseSensitive, boolean isMultiline, boolean doesDotMatchNewline,
			MustacheTemplate name, Regexp[] tests, MustacheTemplate replacement, int match, FindOne[] findsOne, FindMany[] findsMany) {
		super(location, pattern, isCaseSensitive, isMultiline, doesDotMatchNewline, name, tests, replacement);
		this.match = match;
		this.findOnes = findsOne;
		this.findManys = findsMany;
	}
	
	private final FindOne[] findOnes;
	public FindOne[] getFindOnes() {
		return findOnes;
	}
	
	private final FindMany[] findManys;
	public FindMany[] getFindManys() {
		return findManys;
	}
	
	private static final String MATCH = "match";
	
	/**
	 * Defaults to <code>0</code>, the first match.
	 */
	private static final int DEFAULT_MATCH = 0;
}
