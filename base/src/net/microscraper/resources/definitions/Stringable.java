package net.microscraper.resources.definitions;

import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFailure;
import net.microscraper.resources.ScrapingFatality;

/**
 * Classes that implement Stringable may be turned into a String with the addition of 
 * an Scraper.
 * @author john
 *
 */
public interface Stringable {

	/**
	 * Obtain the result for a specific Scraper.
	 * @param context The Scraper to use.
	 * @return The executable's result, if it succeeds.
	 * @throws ScrapingDelay if the Parsable should be tried again later.
	 * @throws ScrapingFailure if the Parsable will not work, but parsing should continue
	 * @throws ScrapingFatality if the Parsable will not work, and parsing should stop.
	 */
	public String getString(Scraper context)
		throws ScrapingDelay, ScrapingFailure, ScrapingFatality;
}
