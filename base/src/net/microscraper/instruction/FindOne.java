package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.uri.URIInterface;
import net.microscraper.util.Variables;

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

	/**
	 * A {@link FindOne} finds a single scraper match. It is
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * Defaults to {@link #DEFAULT_MATCH}.
	 * @see FindMany#minMatch
	 * @see FindMany#maxMatch
	 */
	private final int match;
	
	/**
	 * Deserialize a {@link FindOne} from a {@link JSONObjectInterface}.
	 * @param jsonObject Input {@link JSONObjectInterface} object.
	 * @return A {@link FindOne} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link FindOne}.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public FindOne(JSONObjectInterface jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			this.match = jsonObject.has(MATCH) ? jsonObject.getInt(MATCH) : DEFAULT_MATCH;
		} catch(JSONParserException e) {
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
	
	protected String[] generateResultValues(RegexpCompiler compiler,
			Browser browser, Variables variables, String source)
			throws MissingVariableException, BrowserException, RegexpException {
		return new String[] { matchOne(compiler, source, variables, match) };
	}
}
