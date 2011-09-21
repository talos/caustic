package net.microscraper.regexp;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringSubstitution;
import net.microscraper.util.Encoder;

/**
 * An implementation of {@link RegexpCompiler} is required for microscraper to parse strings.
 * In the spirit of {@link java.util.regex.Pattern#compile(String, int)}, but with a limited set of flags.
 * @see {@link Pattern}
 * @author john
 *
 */
public interface RegexpCompiler {
	/**
	 * Roughly equivalent to {@link java.util.regex.Pattern#compile}.  There are fewer flags,
	 * and they are specified as booleans.
	 * @param patternString The pattern, as a String.
	 * @param isCaseInsensitive <code>False</code> if the pattern should be case-sensitive,
	 * <code>true</code> otherwise.  Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#CASE_INSENSITIVE}.
	 * @param isMultiline <code>True</code> if <code>^</code> and <code>$</code> should match at the start and end of
	 * every line, <code>false</code> otherwise.  Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#MULTILINE}.
	 * @param doesDotMatchNewline <code>True</code> if "<code>.</code>" should match newlines as well,
	 * <code>false</code> otherwise.
	 * Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#DOTALL}.
	 * @return The compiled {@link Pattern}.
	 * @see java.util.regex.Pattern#compile(String, int)
	 * @see java.util.regex.Pattern#CASE_INSENSITIVE
	 * @see java.util.regex.Pattern#MULTILINE
	 * @see java.util.regex.Pattern#DOTALL
	 * @see Pattern
	 */
	public abstract Pattern compile(String patternString,
			boolean isCaseInsensitive, boolean isMultiline,
			boolean doesDotMatchNewline);
	
	/**
	 * Compile a {@link StringTemplate} that will be evalauated by this {@link RegexpCompiler}.
	 * @param templateString The {@link String} template.
	 * @param encodedPattern A {@link String} that will be compiled as a regular expression and used
	 * to identify substitutions that should be encoded in {@link StringTemplate} using <code>
	 * encoder</code>.
	 * @param notEncodedPattern A {@link String} that will be compiled as a regular expression and used
	 * to identify substitutions that should not be encoded in {@link StringTemplate}.
	 * @param encoder The {@link Encoder} to use when encoding tags identified by <code>encodedPattern
	 * </code>.
	 * @return A {@link StringTemplate}.
	 */
	public abstract StringTemplate compileTemplate(String templateString,
			String encodedPatternString, String notEncodedPatternString,
			Encoder encoder);
}