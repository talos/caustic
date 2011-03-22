package net.microscraper.client.interfaces;


/**
 * Equivalent to a compiled java.util.regex.Pattern .
 * @author john
 *
 */
public interface Regexp {
	
	/**
	 * True/false based on whether we find a match.
	 * @param input
	 * @return Whether a match was found.
	 */
	public abstract boolean matches(String input);
	
	/**
	 * Return the match in the first set of parentheses, after a certain number of previous matches; if there are not parentheses,
	 * return the whole pattern.  Returns null if no match, or if there is no match at that matchNumber index.
	 * @param input
	 * @param matchNumber The number of matches to skip.
	 * @return The first grouped match, the entire match, or null.
	 */
	public abstract String match(String input, int matchNumber);
	
	/**
	 * Iterate through the input, and repeatedly provide the match from the first set
	 * of parentheses; if there are not parentheses, this becomes an alias for split(String input).
	 * @param input
	 * @return An array of all the first group matchings, or an array of all the matches,
	 * or null.
	 */
	public abstract String[] allMatches(String input);
	
	public interface Interface {
		/**
		 * Equivalent to java.util.regex.Compile.
		 * @param patternString A pattern string to compile.
		 * @return A GeograpePattern.
		 */
		public abstract Regexp compile(String patternString);
	}
}