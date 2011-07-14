package net.microscraper.server.instruction;

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
	private static final String NAME = "name";
	private static final String REPLACEMENT = "replacement";
	
	/**
	 * By default, carry through the entire match through the find operation.
	 */
	private static final String DEFAULT_REPLACEMENT = "$0";
	private static final String TESTS = "tests";
	
	/**
	 * This string, which is mustached and evaluated for backreferences,
	 * is returned for each match.
	 * Defaults to {@link #DEFAULT_REPLACEMENT}.
	 */
	public final MustacheTemplate replacement;
	
	/**
	 * Patterns that test the sanity of the parser's output.
	 */
	public final Regexp[] tests;
	
	private final MustacheTemplate name;

	/**
	 * @return A {@link MustacheTemplate} attached to this particular {@link Find} {@link Instruction}.
	 * Is <code>null</code> if it has none.
	 * @see {@link #hasName}
	 */
	public final MustacheTemplate getName() {
		return name;
	}
	
	/**
	 * Whether this {@link Find} {@link Instruction} has a {@link #name}.
	 * @see {@link #name}
	 */
	public final boolean hasName() {
		if(getName() == null)
			return false;
		return true;
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
			if(jsonObject.has(NAME)) {
				name = new MustacheTemplate(jsonObject.getString(NAME));
			} else {
				name = null;
			}
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
}
