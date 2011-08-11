package net.microscraper.instruction;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplate;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * A regular expression {@link Instruction}.
 * @author john
 *
 */
public class Regexp {

	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	private final MustacheTemplate pattern;
	
	/**
	 * @return The {@link Regexp}'s pattern.  Mustache compiled before it is used.
	 */
	private final boolean isCaseSensitive;

	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	private final boolean isMultiline;

	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	private final boolean doesDotMatchNewline;
	
	/**
	 * Deserialize a {@link Regexp} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Regexp} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a
	 * {@link Regexp},
	 * or the location is invalid.
	 */
	public Regexp (JSONInterfaceObject jsonObject) throws DeserializationException {
		try {
			pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		} catch(JSONInterfaceException e) {
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
		
	private static final String PATTERN = "pattern";
	
	private static final String IS_CASE_SENSITIVE = "case_sensitive";
	private static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	private static final String IS_MULTILINE = "multiline";
	private static final boolean IS_MULTILINE_DEFAULT = false;

	private static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	private static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;
	

	/**
	 * Compile a {@link Regexp} into a {@link PatternInterface}.
	 * @param compiler The {@link RegexpCompiler} to use.
	 * @param variables The {@link Variables} to use.
	 * @return The {@link PatternInterface}.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 */
	public PatternInterface compile(RegexpCompiler compiler, Variables variables)
			throws MissingVariableException {
		return compiler.compile(
				pattern.compile(variables),
				isCaseSensitive,
				isMultiline, doesDotMatchNewline);
	}
	
	/**
	 * Compile an array of {@link Regexp}s into an array of {@link PatternInterface}s.
	 * @param regexps The array of {@link Regexp}s to compile.
	 * @param variables The {@link Variables} to use.
	 * @return An array of {@link PatternInterface}.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 */
	public static PatternInterface[] compile(Regexp[] regexps, RegexpCompiler compiler, Variables variables)
			throws MissingVariableException {
		PatternInterface[] patterns = new PatternInterface[regexps.length];
		for(int i  = 0 ; i < regexps.length ; i++) {
			patterns[i] = regexps[i].compile(compiler, variables);
		}
		return patterns;
	}
}
