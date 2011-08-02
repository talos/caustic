package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheTemplate;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public class Find extends Regexp {
	
	private static final String REPLACEMENT = "replacement";
	
	/**
	 * By default, <code>$0</code>.  This pulls through the matched string unchanged.
	 */
	private static final String DEFAULT_REPLACEMENT = "$0";
	private static final String TESTS = "tests";
	
	private final MustacheTemplate replacement;
	
	/**
	 * @return The {@link String} that should be mustached and evaluated for backreferences,
	 * then returned once for each match.
	 * Defaults to {@link #DEFAULT_REPLACEMENT}.
	 */
	public final MustacheTemplate getReplacement() {
		return replacement;
	}
	
	private final Regexp[] tests;
	
	/**
	 * @return Patterns that test the sanity of the parser's output.
	 */
	public final Regexp[] getTests() {
		return tests;
	}
	

	/**
	 * Deserialize a {@link Find} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Find} instance.
	 * @throws IOException 
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Find},
	 * or the location is invalid.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Find(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			
			if(jsonObject.has(TESTS)) {
				JSONInterfaceArray tests = jsonObject.getJSONArray(TESTS);
				this.tests = new Regexp[tests.length()];
				for(int i = 0 ; i < this.tests.length ; i ++) {
					this.tests[i] = new Regexp(tests.getJSONObject(i));
				}
			} else {
				this.tests = new Regexp[0];
			}
			this.replacement = jsonObject.has(REPLACEMENT) ?
					new MustacheTemplate(jsonObject.getString(REPLACEMENT)) :
					new MustacheTemplate(DEFAULT_REPLACEMENT);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public Find(JSONLocation location, MustacheTemplate name, MustacheTemplate pattern, boolean isCaseSensitive, boolean isMultiline,
			boolean doesDotMatchNewline, Regexp[] tests, MustacheTemplate replacement) {
		super(location, name, pattern, isCaseSensitive, isMultiline, doesDotMatchNewline);
		this.tests = tests;
		this.replacement = replacement;
	}
}
