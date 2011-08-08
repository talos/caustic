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
	
	private final MustacheTemplate pattern;
	
	/**
	 * @return The {@link Regexp}'s pattern.  Mustache compiled before it is used.
	 */
	public final MustacheTemplate getPattern() {
		return pattern;
	}
	
	private final boolean isCaseSensitive;
	
	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	public final boolean getIsCaseSensitive() {
		return isCaseSensitive;
	}

	private final boolean isMultiline;

	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	public final boolean getIsMultiline() {
		return isMultiline;
	}
	
	private final boolean doesDotMatchNewline;
	/**
	 * @see net.microscraper.interfaces.json.JSONInterface#compile
	 */
	public final boolean getDoesDotMatchNewline() {
		return doesDotMatchNewline;
	}
	
	/**
	 * Deserialize a {@link Regexp} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Regexp} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a
	 * {@link Regexp},
	 * or the location is invalid.
	 */
	public Regexp (JSONInterfaceObject jsonObject) throws DeserializationException {
		//super(jsonObject);
		try {
			pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		} catch(JSONInterfaceException e) {
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
	 * 
	 * @return The {@link RegexpExecutable}'s {@link PatternInterface}
	 * to use when executing.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 * @throws MustacheTemplateException if the {@link MustacheTemplate} for
	 * the pattern is invalid.
	 */
	/*
	public PatternInterface getPattern(Variables variables)
			throws MissingVariableException, MustacheTemplateException {
		return regexpCompiler.compile(
				regexpInstruction.getPattern().compile(variables),
				regexpInstruction.getIsCaseSensitive(),
				regexpInstruction.getIsMultiline(), regexpInstruction.getDoesDotMatchNewline());
	}
	*/
	public PatternInterface compileWith(RegexpCompiler regexpCompiler,
			Variables variables) throws MissingVariableException, MustacheTemplateException {
		return regexpCompiler.compile(
				getPattern().compile(variables),
				getIsCaseSensitive(),
				getIsMultiline(), getDoesDotMatchNewline());
	}
}
