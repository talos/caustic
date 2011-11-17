package net.caustic.regexp;

/**
 * Exception to indicate that a {@link Pattern} range is invalid.
 * @author realest
 *
 */
public class InvalidRangeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8792952974978095125L;
	//private final int minMatch;
	//private final int maxMatch;
	public InvalidRangeException(Pattern pattern, int minMatch, int maxMatch) {
		super("Range " + minMatch + " to " + maxMatch + " is not valid.");
		//this.minMatch = minMatch;
		//this.maxMatch = maxMatch;
	}
}