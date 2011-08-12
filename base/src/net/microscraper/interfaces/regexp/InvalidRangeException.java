package net.microscraper.interfaces.regexp;

/**
 * Exception to indicate that a {@link PatternInterface} range is invalid.
 * @author realest
 *
 */
public class InvalidRangeException extends RegexpException {
	private final int minMatch;
	private final int maxMatch;
	public InvalidRangeException(PatternInterface pattern, int minMatch, int maxMatch) {
		super(pattern, "Range " + minMatch + " to " + maxMatch + " is not valid.");
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
	}
}