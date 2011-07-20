package net.microscraper.interfaces.regexp;

/**
 * An implementation of {@link PatternInterface} is required for {@link RegexpCompiler}.
 * Follows the conventions of {@link java.util.regex.Pattern}.
 * @see RegexpCompiler
 * @author john
 *
 */
public interface PatternInterface {
	/**
	 * Returns True/false based on whether we find a match at any point in the input.
	 * @param input String input
	 * @return {@link boolean} Whether a match was found.
	 */
	public abstract boolean matches(String input);
	
	/**
	 * Returns True/false based on whether we find a match at the specified matchNumber.
	 * @param input String input
	 * @param matchNumber
	 * @return {@link boolean} Whether a match was found.
	 */
	public abstract boolean matches(String input, int matchNumber);
	
	/**
	 * Returns a String of the substitution at matchNumber.
	 * @param input String input
	 * @param substitution The substitution to use, for example "$0"
	 * @param matchNumber Which match to use in the substitution. 0-indexed.
	 * @return {@link String} A string of the substitution at matchNumber.
	 * @throws NoMatchesException There was no match at the match number for this pattern.
	 * @throws MissingGroupException The substitution referred to a backreference group not in the pattern.
	 */
	public abstract String match(String input, String substitution, int matchNumber) throws NoMatchesException, MissingGroupException;
	
	/**
	 * Returns an array of Strings of the substitution, one for each match.
	 * @param input String input
	 * @param substitution The substitution to use, for example "$0"
	 * @param minMatch Which match should be the first to be included in the return.  0-indexed, with negative numbers counting backwards from end (-1 is last).
	 * @param maxMatch Which match should be the last to be included in the return.  0-indexed, with negative numbers counting backwards from end (-1 is last.
	 * @return {@link String[]} An array of strings, each using the substitution for the pattern.
	 * @throws NoMatchesException There was no match at the match number for this pattern.
	 * @throws MissingGroupException The substitution referred to a backreference group not in the pattern.
	 * @throws InvalidRangeException The range referred to a positive maximum less than a positive minimum, or a negative maximum less than a negative minimum.
	 */
	public abstract String[] allMatches(String input, String substitution, int minMatch, int maxMatch) throws NoMatchesException, MissingGroupException, InvalidRangeException;
}