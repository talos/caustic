package net.microscraper.resources.definitions;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * Classes that implement Stringable may be turned into a String with the addition of 
 * an ExecutionContext.
 * @author john
 *
 */
public interface Stringable {

	/**
	 * Obtain the result for a specific ExecutionContext.
	 * @param context The ExecutionContext to use.
	 * @return The executable's result, if it succeeds.
	 * @throws ExecutionDelay if the Parsable should be tried again later.
	 * @throws ExecutionFailure if the Parsable will not work, but parsing should continue
	 * @throws ExecutionFatality if the Parsable will not work, and parsing should stop.
	 */
	public String getString(ExecutionContext context)
		throws ExecutionDelay, ExecutionFailure, ExecutionFatality;
}
