package net.microscraper.client;

import net.microscraper.database.DatabaseView;
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
	private DatabaseView[] views;
	
	private ScraperResult(String name, DatabaseView[] views, Scraper[] children) {
		super((Object) children); // force 'successful' constructor.
		this.name = name;
		this.views = views;
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
	 * @return An array of {@link DatabaseView}s created from the scrape.
	 */
	public DatabaseView[] getResultViews() {
		getSuccess();
		return views;
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
	 * @param resultViews An array of {@link DatabaseView} values, accessible through
	 * {@link #getResultViews()}.
	 * @param children An array of {@link Scraper} children, accessible through
	 * {@link #getChildren()}.
	 * @return A successful {@link ScraperResult}.
	 */
	public static ScraperResult success(String name, DatabaseView[] resultViews, Scraper[] children) {
		return new ScraperResult(name, resultViews, children);
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
