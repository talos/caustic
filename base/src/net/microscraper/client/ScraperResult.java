package net.microscraper.client;

import net.microscraper.database.DatabaseView;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.util.Result;

/**
 * This class provides access to the results of scraping
 * @author realest
 * @see Scraper#scrape()
 * @see Instruction#execute(String, net.microscraper.util.StringMap)
 *
 */
public class ScraperResult implements Result {
	private String[] missingTags;
	private String failedBecause;
	private String name;
	private DatabaseView[] views;
	private Scraper scraperToRetry;
	private Scraper[] children;
	private boolean isSuccess = false;
	
	private ScraperResult(String name, DatabaseView[] views, Scraper[] children) {
		this.isSuccess = true;
		this.name = name;
		this.views = views;
		this.children = children;
	}
	
	private ScraperResult(String[] missingTags, Scraper scraperToRetry) {
		this.missingTags = missingTags;
		this.scraperToRetry = scraperToRetry;
	}
	
	private ScraperResult(String failedBecause) {
		this.failedBecause = failedBecause;
	}
	
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	/**
	 * 
	 * @return The {@link String} name attached to the results of the scrape.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return An array of {@link DatabaseView}s created from the scrape.
	 */
	public DatabaseView[] getResultViews() {
		return views;
	}
	
	/**
	 * 
	 * @return An array of {@link Scraper}s that should be launched because
	 * of the successful scrape.
	 * Should only be called when {@link #isSuccess()}
	 * is <code>true</code>
	 */
	public Scraper[] getChildren() {
		return children;
	}
	
	/**
	 * 
	 * @return The {@link Scraper} that should be retried because its previous
	 * scrape was stopped by missing tags.
	 * Should only be called when {@link #isMissingTags()}
	 * is <code>true</code>
	 */
	public Scraper getScraperToRetry() {
		getMissingTags();
		return scraperToRetry;
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
	 * @param scraperToRetry The {@link Scraper} that could be retried once
	 * more tags are available.
	 * @return A {@link ScraperResult} with missing tag information.
	 */
	public static ScraperResult missingTags(String[] missingTags, Scraper scraperToRetry) {
		return new ScraperResult(missingTags, scraperToRetry);
	}

	public static ScraperResult failed(InstructionResult failedInstructionResult) {
		return new ScraperResult(failedInstructionResult.getFailedBecause());
	}

	public boolean isMissingTags() {
		return missingTags != null;
	}

	public String[] getMissingTags() {
		return missingTags;
	}

	public String getFailedBecause() {
		return failedBecause;
	}
}
