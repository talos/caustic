package net.microscraper.client;

import net.microscraper.instruction.Instruction;
import net.microscraper.util.Result;

/**
 * This class provides access to the results of scraping
 * @author realest
 * @see Scraper#scrape()
 * @see Instruction#execute(String, net.microscraper.util.StringMap)
 *
 */
public class ScraperResult extends Result {
	private String name;
	private String[] values;
	
	private ScraperResult(String name, String[] values, Scraper[] children) {
		super((Object) children); // force 'successful' constructor.
		this.name = name;
		this.values = values;
	}
	
	private ScraperResult(String[] missingTags) {
		super(missingTags);
	}
	
	private ScraperResult(String failedBecause) {
		super(failedBecause);
	}
	
	/**
	 * 
	 * @return The {@link String} name attached to the results of the scrape.
	 */
	public String getName() {
		getSuccess();
		return name;
	}
	
	/**
	 * 
	 * @return An array of {@link String} values that are the results of a 
	 * scrape.
	 */
	public String[] getValues() {
		getSuccess();
		return values;
	}
	
	/**
	 * 
	 * @return An array of {@link Scraper}s that should be launched because
	 * of the successful scrape.
	 */
	public Scraper[] getChildren() {
		return (Scraper[]) getSuccess();
	}
	
	/**
	 * Obtain a successful {@link ScraperResult}.
	 * @param name The {@link String} name, accessible through {@link #getName()}
	 * @param values An array of {@link String} values, accessible through
	 * {@link #getValues()}.
	 * @param children An array of {@link Scraper} children, accessible through
	 * {@link #getChildren()}.
	 * @return A successful {@link ScraperResult}.
	 */
	public static ScraperResult success(String name, String[] values, Scraper[] children) {
		return new ScraperResult(name, values, children);
	}
	
	/**
	 * Obtain a {@link ScraperResult} with missing tag information.
	 * @param missingTags A {@link String} array of the tags that prevented
	 * a successful scrape.
	 * @return A {@link ScraperResult} with missing tag information.
	 */
	public static ScraperResult missingTags(String[] missingTags) {
		return new ScraperResult(missingTags);
	}

	/**
	 * Obtain a {@link ScraperResult} with failure information.
	 * @param failure A {@link String} describing why the scrape failed,
	 * and should not be tried again.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static ScraperResult failure(String failedBecause) {
		return new ScraperResult(failedBecause);
	}
}
