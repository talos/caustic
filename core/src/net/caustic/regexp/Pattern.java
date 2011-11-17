package net.caustic.regexp;

/**
 * An implementation of {@link Pattern} is required for {@link RegexpCompiler}.
 * @see RegexpCompiler
 * @author john
 *
 */
public interface Pattern {
	
	/**
	 * The {@link int} corresponding to the first match of {@link #match(String, String, int, int)}.
	 * Positive numbers count forwards from this first match.
	 */
	public static final int FIRST_MATCH = 0;
	
	/**
	 * The {@link int} corresponding to the last match of {@link #match(String, String, int, int)}.
	 * Negative numbers count backwards from this last match.
	 */
	public static final int LAST_MATCH = -1;
	
	/**
	 * Returns True/false based on whether we find a match at the specified <code>matchNumber</code>.
	 * @see #LAST_MATCH
	 * @see #FIRST_MATCH
	 * @param input String input
	 * @param matchNumber The number of matches to look for before returning <code>true</code>.  Must
	 * be 0 or positive.
	 * @return {@link boolean} Whether a match was found at the specified <code>matchNumber</code>.
	 * @throws InvalidRangeException if <code>matchNumber</code> is negative.
	 */
	public abstract boolean matches(String input, int matchNumber);
	
	/**
	 * Returns an array of Strings of the substitution, one for each match.  Returns a zero-length
	 * array if there were no matches.
	 * @param input {@link String} input.
	 * @param replacement The replacement to use.  Backreferences are notated with
	 * <code>$</code>, so <code>$0</code> would be the whole match, <code>$1</code>
	 * the first backreference, etc.
	 * @param minMatch Which match should be the first to be included in the return.  0-indexed, with negative numbers counting backwards from end (-1 is last).
	 * @param maxMatch Which match should be the last to be included in the return.  0-indexed, with negative numbers counting backwards from end (-1 is last.
	 * @return {@link String[]} An array of strings, each using the substitution for the pattern,
	 * of zero length if there were no matches.
	 * @see #LAST_MATCH
	 * @see #FIRST_MATCH
	 */
	public abstract String[] match(String input, String replacement, int minMatch, int maxMatch);

}