package net.microscraper.server.resource;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheTemplate;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public class Find extends Regexp {
	private static final String REPLACEMENT = "replacement";
	private static final String TESTS = "tests";
	
	/**
	 * This string, which is mustached and evaluated for backreferences,
	 * is returned for each match.
	 */
	public final MustacheTemplate replacement;
	
	/**
	 * Patterns that test the sanity of the parser's output.
	 */
	public final Regexp[] tests;
	
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
			this.replacement = new MustacheTemplate(jsonObject.getString(REPLACEMENT));
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
