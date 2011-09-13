package net.microscraper.client;

import net.microscraper.database.DatabaseView;
import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.http.HttpException;
import net.microscraper.instruction.Instruction;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.HashtableSubstitutionOverwriteException;
import net.microscraper.util.Result;
import net.microscraper.util.StringUtils;

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
	 * Failed because of an HTTP exception.
	 * @param e A {@link HttpException} that caused this scrape to
	 * fail.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static ScraperResult fromHttpException(HttpException e) {
		return new ScraperResult("Failure during HTTP request or response: " + e.getMessage());
	}

	/**
	 * Failed because of substitution causing an ambiguous mapping.
	 * @param e A {@link HashtableSubstitutionOverwriteException} of the overwrite.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static ScraperResult fromSubstitutionOverwrite(
			HashtableSubstitutionOverwriteException e) {
		return new ScraperResult("Instruction template substitution caused ambiguous mapping: "
			+ e.getMessage());
	}

	/**
	 * Obtain a {@link ScraperResult} with failure information.
	 * @param pattern
	 * @param minMatch
	 * @param maxMatch
	 * @param source
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static ScraperResult noMatchesFailure(Pattern pattern, int minMatch,
			int maxMatch, String source) {
		 return new ScraperResult("Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.quoteAndTruncate(StringUtils.quote(source), 100));
	}
	

	/**
	 * Failed because of a deserialization error.
	 * @param e A failed {@link DeserializerResult}.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static ScraperResult fromDeserializerFailure(
			DeserializerResult result) {
		return new ScraperResult("Failed because of deserialization error: "
			+ result.getFailedBecause());
	}
}
