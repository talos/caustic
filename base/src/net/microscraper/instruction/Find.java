package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheTemplateException;
import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.MissingGroupException;
import net.microscraper.regexp.NoMatchesException;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.uri.URIInterface;
import net.microscraper.util.Variables;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public abstract class Find extends Instruction {
	
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

	/**
	 * The {@link String} that should be mustached and evaluated for backreferences,
	 * then returned once for each match.
	 * Defaults to {@link #DEFAULT_REPLACEMENT}.
	 */
	private final MustacheTemplate replacement;

	/**
	 * {@link Regexp}s that test the sanity of the parser's output.  Defaults to a 
	 * {@link #DEFAULT_TESTS}.
	 */
	private final Regexp[] tests;
	
	/**
	 * By default, {@link getTests()} is a zero-length {@link Regexp} array.
	 */
	public static final Regexp[] DEFAULT_TESTS = new Regexp[] {};
	
	/**
	 * Deserialize a {@link Find} from a {@link JSONObjectInterface}.
	 * @param jsonObject Input {@link JSONObjectInterface} object.
	 * @return A {@link Find} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Find},
	 * or the location is invalid.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Find(JSONObjectInterface jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			regexp = new Regexp(jsonObject);
			if(jsonObject.has(TESTS)) {
				JSONArrayInterface tests = jsonObject.getJSONArray(TESTS);
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
		} catch(JSONParserException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheTemplateException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	/**
	 * 
	 * The {@link Regexp} {@link Instruction} inside this {@link Find}.
	 */
	private final Regexp regexp;
	
	/**
	 * {@link Find} saves its value by default.
	 */
	public boolean defaultShouldSaveValue() {
		return true;
	}
	
	protected String[] matchMany(RegexpCompiler compiler, String source, Variables variables, int minMatch, int maxMatch)
			throws MissingGroupException, InvalidRangeException, MissingVariableException, NoMatchesException {		
		return regexp.compile(compiler, variables).allMatches(
				source,
				replacement.compile(variables),
				minMatch, maxMatch);
	}
	
	protected String matchOne(RegexpCompiler compiler, String source, Variables variables, int match)
			throws MissingGroupException, InvalidRangeException, MissingVariableException, NoMatchesException {		
		return regexp.compile(compiler, variables).match(
				source,
				replacement.compile(variables),
				match);
	}
	
	public String getDefaultName(Variables variables, RegexpCompiler compiler, Browser browser)
			throws MissingVariableException, RegexpException {
		return regexp.compile(compiler, variables).toString();
	}
}
