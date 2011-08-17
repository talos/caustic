package net.microscraper.mustache;

import net.microscraper.instruction.DeserializationException;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonObject;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Variables;

/**
 * A {@link Pattern} that uses a {@link MustacheTemplate} for substitutions.
 * @author john
 *
 */
public class MustachePattern {

	/**
	 * @see net.microscraper.json.JsonParser#compile
	 */
	private final MustacheTemplate pattern;
	
	/**
	 * @return The {@link MustachePattern}'s pattern.  Mustache compiled before it is used.
	 */
	private final boolean isCaseSensitive;

	/**
	 * @see net.microscraper.json.JsonParser#compile
	 */
	private final boolean isMultiline;

	/**
	 * @see net.microscraper.json.JsonParser#compile
	 */
	private final boolean doesDotMatchNewline;
	
	public MustachePattern(MustacheTemplate pattern, boolean isCaseSensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		this.pattern = pattern;
		this.isCaseSensitive = isCaseSensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
	

	/**
	 * Compile a {@link MustachePattern} into a {@link Pattern}.
	 * @param compiler The {@link RegexpCompiler} to use.
	 * @param variables The {@link Variables} to use.
	 * @return The {@link Pattern}.
	 */
	public Pattern compile(RegexpCompiler compiler, Variables variables) {
		return compiler.compile(
				pattern.sub(variables),
				isCaseSensitive,
				isMultiline, doesDotMatchNewline);
	}
	
	/**
	 * Compile an array of {@link MustachePattern}s into an array of {@link Pattern}s.
	 * @param regexps The array of {@link MustachePattern}s to compile.
	 * @param variables The {@link Variables} to use.
	 * @return An array of {@link Pattern}.
	 */
	public static Pattern[] compile(MustachePattern[] regexps, RegexpCompiler compiler, Variables variables) {
		Pattern[] patterns = new Pattern[regexps.length];
		for(int i  = 0 ; i < regexps.length ; i++) {
			patterns[i] = regexps[i].compile(compiler, variables);
		}
		return patterns;
	}
}
