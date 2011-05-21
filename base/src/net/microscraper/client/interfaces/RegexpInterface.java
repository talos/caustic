package net.microscraper.client.interfaces;

/**
 * An implementation of {@link RegexpInterface} is required for microscraper to parse strings.
 * In the spirit of {@link java.util.regex.Pattern#compile(String, int)}, but with a limited set of flags.
 * @see {@link PatternInterface}
 * @author john
 *
 */
public interface RegexpInterface {
	/**
	 * Roughly equivalent to {@link java.util.regex.Pattern#compile}.  There are fewer flags,
	 * and they are specified as booleans.
	 * @param patternString The pattern, as a String.
	 * @param isCaseInsensitive <code>True</code> if the pattern should be case-sensitive,
	 * <code>false</code> otherwise.  Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#CASE_INSENSITIVE}.
	 * @param isMultiline <code>True</code> if <code>^</code> and <code>$</code> should match at the start and end of
	 * every line, <code>false</code> otherwise.  Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#MULTILINE}.
	 * @param doesDotMatchNewline <code>True</code> if "<code>.</code>" should match newlines as well,
	 * <code>false</code> otherwise.
	 * Should work in a manner equivalent to
	 * {@link java.util.regex.Pattern#DOTALL}.
	 * @return The compiled {@link PatternInterface}.
	 * @see java.util.regex.Pattern#compile(String, int)
	 * @see java.util.regex.Pattern#CASE_INSENSITIVE
	 * @see java.util.regex.Pattern#MULTILINE
	 * @see java.util.regex.Pattern#DOTALL
	 * @see PatternInterface
	 */
	public abstract PatternInterface compile(String patternString,
			boolean isCaseInsensitive, boolean isMultiline,
			boolean doesDotMatchNewline);
	
}