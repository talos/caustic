package net.caustic.regexp;

public class RegexpUtils {
	
	/**
	 * Static method to determine whether two {@link int} would form a valid range for
	 * {@link Pattern#match(String, String, int, int)}.<p>
	 * A range is valid if the two bounds have opposite signs.<p>
	 * A range is valid if the two bounds are positive, and the maximum is greater than
	 * or equal to than the minimum.<p>
	 * A range is valid if the two bounds are negative, and the minimum is less than 
	 * or equal to the maximum.<p>
	 * A range is invalid otherwise.
	 * @param minMatch The {@link int} lower bound to test.
	 * @param maxMatch The {@link int} upper bound to test.
	 * @return <code>True</code> if it would be a valid range, <code>false</code> otherwise.
	 */
	public static boolean isValidRange(int minMatch, int maxMatch) {
		if(minMatch >= 0 && maxMatch < 0) {
			return true;
		}
		if(minMatch < 0 && maxMatch >= 0) {
			return true;
		}
		if(minMatch >= 0 && maxMatch >= 0 && maxMatch >= minMatch) {
			return true;
		}
		if(minMatch <  0 && maxMatch < 0  && minMatch <= maxMatch) {
			return true;
		}
		return false;
	}
}
