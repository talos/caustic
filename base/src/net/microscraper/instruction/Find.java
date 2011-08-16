package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.MissingGroupException;
import net.microscraper.regexp.NoMatchesException;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public class Find extends Instruction {
	
	/**
	 * Key for {@link #replacement} value deserializing from JSON.
	 */
	public static final String REPLACEMENT = "replacement";
	
	/**
	 * Default value for {@link #replacement} is the entire match.
	 */
	public static final String ENTIRE_MATCH = "$0";
	
	/**
	 * Key for {@link #getTests()} value deserializing from JSON.
	 */
	public static final String TESTS = "tests";

	/**
	 * The {@link String} that should be mustached and evaluated for backreferences,
	 * then returned once for each match.<p>
	 * Defaults to {@link #ENTIRE_MATCH}.
	 */
	private final MustacheTemplate replacement;

	/**
	 * {@link Regexp}s that test the sanity of the parser's output.  Defaults to 
	 * {@link #NO_TESTS}.
	 */
	private final Regexp[] tests;
	
	/**
	 * By default, {@link #tests} is a zero-length {@link Regexp} array.
	 */
	public static final Regexp[] NO_TESTS = new Regexp[] {};
	
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
				this.tests = NO_TESTS;
			}
			this.replacement = jsonObject.has(REPLACEMENT) ?
					new MustacheTemplate(jsonObject.getString(REPLACEMENT)) :
					new MustacheTemplate(ENTIRE_MATCH);
			
			if(jsonObject.has(MATCH)) {
				if(jsonObject.has(MIN_MATCH) || jsonObject.has(MAX_MATCH)) {
					throw new DeserializationException("Cannot define max or min when defining a match." , jsonObject);
				}
				minMatch = jsonObject.getInt(MATCH);
				maxMatch = jsonObject.getInt(MATCH);
			} else {
				minMatch = jsonObject.has(MIN_MATCH) ? jsonObject.getInt(MIN_MATCH) : FIRST_MATCH;
				maxMatch = jsonObject.has(MAX_MATCH) ? jsonObject.getInt(MAX_MATCH) : LAST_MATCH;
			}
			
			if(!RegexpUtils.isValidRange(minMatch, maxMatch)) {
				throw new DeserializationException(StringUtils.quote(minMatch) + " and " + StringUtils.quote(maxMatch) +
						" form invalid range.", jsonObject);
			}
		} catch(JSONParserException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheCompilationException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	/**
	 * 
	 * The {@link Regexp} inside this {@link Find}.
	 */
	private final Regexp regexp;
	
	/**
	 * {@link Find} saves its value by default.
	 */
	public boolean defaultShouldSaveValue() {
		return true;
	}
	
	/**
	 * {@link Find}'s name is a {@link Mustache} compiled version of its {@link #regexp}.
	 */
	public String getDefaultName(Variables variables, RegexpCompiler compiler, Browser browser)
			throws MissingVariableException, RegexpException {
		return regexp.compile(compiler, variables).toString();
	}
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * <p>
	 * Defaults to {@link  #FIRST_MATCH}.
	 * @see #maxMatch
	 * @see #generateResultValues(RegexpCompiler, Browser, Variables, String)
	 */
	private final int minMatch;

	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * <p>
	 * Defaults to {@link #LAST_MATCH}.
	 * @see #minMatch
	 * @see #generateResultValues(RegexpCompiler, Browser, Variables, String)
	 */
	private final int maxMatch;
	
	/**
	 * Key for {@link #minMatch} value when deserializing from JSON.
	 */
	public static final String MIN_MATCH = "min";
	
	/**
	 * Key for {@link #maxMatch} value when deserializing from JSON.
	 */
	public static final String MAX_MATCH = "max";
	
	/**
	 * {@link #minMatch} defaults to the first of any number of matches.
	 */
	public static final int FIRST_MATCH = 0;
	
	/**
	 * {@link #getMaxMatch()} defaults to the last of any number of matches.
	 */
	public static final int LAST_MATCH = -1;
	
	public String[] generateResultValues(RegexpCompiler compiler,
			Browser browser, Variables variables, String source)
					throws NoMatchesException, MissingGroupException,
					InvalidRangeException, MissingVariableException {
		return regexp.compile(compiler, variables).match(
				source,
				replacement.sub(variables),
				minMatch, maxMatch);
	}
	
	/**
	 * If this maps to an {@link int}, then {@link #maxMatch} and {@link #minMatch} are that
	 * value.<p>
	 */
	public static final String MATCH = "match";
}
