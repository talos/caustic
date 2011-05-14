package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * Classes that implement parsable may be used as input in a Parser.
 * @author john
 *
 */
public interface Parsable extends Executable {

	/**
	 * Obtain a string by parsing.
	 * @param context
	 * @return The executable's result, if it succeeds.
	 * @throws ExecutionDelay if the Parsable should be tried again later.
	 * @throws ExecutionFailure if the Parsable will not work, but parsing should continue
	 * @throws ExecutionFatality if the Parsable will not work, and parsing should stop.
	 */
	public String parse(ExecutionContext context)
		throws ExecutionDelay, ExecutionFailure, ExecutionFatality;
}
