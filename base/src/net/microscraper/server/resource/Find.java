package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Resource;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public class Find extends Regexp {

	/**
	 * This string, which is mustached and evaluated for backreferences, is returned for each match.
	 */
	public final MustacheTemplate replacement;
	
	/**
	 * Patterns that test the sanity of the parser's output.
	 */
	public final Regexp[] tests;
	
	public Find(Regexp regexp, MustacheTemplate replacement, Regexp[] tests) throws URIMustBeAbsoluteException {
		super(regexp.location, regexp.pattern, regexp.isCaseInsensitive, regexp.isMultiline, regexp.doesDotMatchNewline);
		this.replacement = replacement;
		this.tests = tests;
	}
	
	public Find(Find find) throws URIMustBeAbsoluteException {
		super(find.location, find.pattern, find.isCaseInsensitive, find.isMultiline, find.doesDotMatchNewline);
		this.replacement = find.replacement;
		this.tests = find.tests;
	}

	private static final String REPLACEMENT = "replacement";
	private static final String TESTS = "tests";
	
	/**
	 * Deserialize a {@link Find} from a {@link JSONInterfaceObject}.
	 * @return A {@link Find} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Find}.
	 */
	public static Resource deserialize(JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Regexp regexp = (Regexp) Regexp.deserialize(jsonObject);
			MustacheTemplate replacement = new MustacheTemplate(jsonObject.getString(REPLACEMENT));
			Regexp[] tests = Regexp.deserializeArray(jsonObject.getJSONArray(TESTS));
			
			return new Find(regexp, replacement, tests);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
