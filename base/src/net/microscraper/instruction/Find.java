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
public class Find extends Instruction {
	
	/**
	 * Key for {@link #getReplacement()} value deserializing from JSON.
	 */
	public static final String REPLACEMENT = "replacement";
	
	/**
	 * Default value for {@link #getReplacement()} is <code>$0</code>
	 *  This pulls through the matched string unchanged.
	 */
	public static final String DEFAULT_REPLACEMENT = "$0";
	
	/**
	 * Key for {@link #getTests()} value deserializing from JSON.
	 */
	public static final String TESTS = "tests";
	
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
	 * @return {@link Regexp}s that test the sanity of the parser's output.  Defaults to a 
	 * {@link #DEFAULT_TESTS}.
	 */
	public final Regexp[] getTests() {
		return tests;
	}
	
	/**
	 * By default, {@link getTests()} is a zero-length {@link Regexp} array.
	 */
	public static final Regexp[] DEFAULT_TESTS = new Regexp[] {};
	
	/**
	 * Deserialize a {@link Find} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Find} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Find},
	 * or the location is invalid.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Find(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			regexp = new Regexp(jsonObject);
			if(jsonObject.has(TESTS)) {
				JSONInterfaceArray tests = jsonObject.getJSONArray(TESTS);
				this.tests = new Regexp[tests.length()];
				for(int i = 0 ; i < this.tests.length ; i ++) {
					this.tests[i] = new Regexp(tests.getJSONObject(i));
				}
			} else {
				this.tests = DEFAULT_TESTS;
			}
			this.replacement = jsonObject.has(REPLACEMENT) ?
					new MustacheTemplate(jsonObject.getString(REPLACEMENT)) :
					new MustacheTemplate(DEFAULT_REPLACEMENT);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public Find(JSONLocation location, MustacheTemplate name,
			boolean shouldSaveValue,
			FindOne[] findOnes, FindMany[] findManys,
			Page[] spawnPages, Regexp regexp, Regexp[] tests,
			MustacheTemplate replacement) {
		super(location, name, shouldSaveValue, findOnes, findManys, spawnPages);
		this.regexp = regexp;
		this.tests = tests;
		this.replacement = replacement;
	}
	
	private final Regexp regexp;
	/**
	 * 
	 * @return The {@link Regexp} {@link Instruction} inside this {@link Find}.
	 */
	public Regexp getRegexp() {
		return regexp;
	}

	/**
	 * {@link Find} saves its value by default.
	 */
	public boolean defaultShouldSaveValue() {
		return true;
	}
}
