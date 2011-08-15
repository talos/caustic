package net.microscraper.regexp;

/**
 * Exception to indicate that a {@link Pattern} range is invalid.
 * @author realest
 *
 */
public class InvalidRangeException extends RegexpException {
	private final int minMatch;
	private final int maxMatch;
	public InvalidRangeException(Pattern pattern, int minMatch, int maxMatch) {
		super(pattern, "Range " + minMatch + " to " + maxMatch + " is not valid.");
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
	}
}