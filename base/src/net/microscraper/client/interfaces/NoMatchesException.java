package net.microscraper.client.interfaces;

import net.microscraper.client.Utils;

/**
 * Exception to indicate that the pattern did not match against its input string.
 * @author john
 *
 */
public class NoMatchesException extends Exception {
	private static final String MATCH = "Match ";
	private static final String MATCHES_BETWEEN = "Matches between ";
	private static final String AND = " and ";
	private static final String NOT_FOUND = " not found in the ";
	private static final String MATCHES_OF = " matches of ";
	private static final String AGAINST = " against ";
	public NoMatchesException(PatternInterface pattern, int numFound, int match, String string) {
		super(MATCH + Utils.quote(match) + NOT_FOUND +
				Utils.quote(numFound) + MATCHES_OF +
				Utils.quote(pattern.toString()) + AGAINST +
				Utils.quote(string));
	}
	public NoMatchesException(PatternInterface pattern, int numFound, int min, int max, String string) {
		super(MATCHES_BETWEEN + Utils.quote(min) + AND +
				Utils.quote(max) + NOT_FOUND +
				Utils.quote(numFound) + MATCHES_OF +
				Utils.quote(pattern.toString()) + AGAINST +
				Utils.quote(string));
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1808377327875482874L;
	
}