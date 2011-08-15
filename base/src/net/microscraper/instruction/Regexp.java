package net.microscraper.instruction;

import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheTemplateException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Variables;

/**
 * A regular expression {@link Instruction}.
 * @author john
 *
 */
public class Regexp {

	/**
	 * @see net.microscraper.json.JSONParser#compile
	 */
	private final MustacheTemplate pattern;
	
	/**
	 * @return The {@link Regexp}'s pattern.  Mustache compiled before it is used.
	 */
	private final boolean isCaseSensitive;

	/**
	 * @see net.microscraper.json.JSONParser#compile
	 */
	private final boolean isMultiline;

	/**
	 * @see net.microscraper.json.JSONParser#compile
	 */
	private final boolean doesDotMatchNewline;
	
	/**
	 * Deserialize a {@link Regexp} from a {@link JSONObjectInterface}.
	 * @param jsonObject Input {@link JSONObjectInterface} object.
	 * @return A {@link Regexp} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a
	 * {@link Regexp},
	 * or the location is invalid.
	 */
	public Regexp (JSONObjectInterface jsonObject) throws DeserializationException {
		try {
			pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		} catch(JSONParserException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheTemplateException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public Regexp(MustacheTemplate pattern, boolean isCaseSensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		this.pattern = pattern;
		this.isCaseSensitive = isCaseSensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
		
	/**
	 * Key for deserializing {@link #pattern}.
	 */
	public static final String PATTERN = "pattern";
	
	/**
	 * Key for deserializing {@link #isCaseSensitive}.
	 */
	public static final String IS_CASE_SENSITIVE = "case_sensitive";
	
	private static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	/**
	 * Key for deserializing {@link #isMultiline}.
	 */
	public static final String IS_MULTILINE = "multiline";
	
	private static final boolean IS_MULTILINE_DEFAULT = false;
	
	/** 
	 * Key for deserializing {@link #doesDotMatchNewline}.
	 */
	public static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	
	private static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;
	

	/**
	 * Compile a {@link Regexp} into a {@link Pattern}.
	 * @param compiler The {@link RegexpCompiler} to use.
	 * @param variables The {@link Variables} to use.
	 * @return The {@link Pattern}.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 */
	public Pattern compile(RegexpCompiler compiler, Variables variables)
			throws MissingVariableException {
		return compiler.compile(
				pattern.compile(variables),
				isCaseSensitive,
				isMultiline, doesDotMatchNewline);
	}
	
	/**
	 * Compile an array of {@link Regexp}s into an array of {@link Pattern}s.
	 * @param regexps The array of {@link Regexp}s to compile.
	 * @param variables The {@link Variables} to use.
	 * @return An array of {@link Pattern}.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 */
	public static Pattern[] compile(Regexp[] regexps, RegexpCompiler compiler, Variables variables)
			throws MissingVariableException {
		Pattern[] patterns = new Pattern[regexps.length];
		for(int i  = 0 ; i < regexps.length ; i++) {
			patterns[i] = regexps[i].compile(compiler, variables);
		}
		return patterns;
	}
}
