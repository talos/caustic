package net.microscraper.instruction;

import net.microscraper.client.ScraperResult;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.Result;
import net.microscraper.util.StringUtils;

public class FindResult implements Result {
	private String name;
	private String[] matches;
	private String[] missingTags;
	private String failedBecause;
	private boolean shouldStoreValues;
	
	private FindResult(String name, String[] matches, boolean shouldStoreValues) {
		this.name = name;
		this.matches = matches;
		this.shouldStoreValues = shouldStoreValues;
	}
	
	private FindResult(String[] missingTags) {
		this.missingTags = missingTags;
	}
	
	private FindResult(String failedBecause) {
		this.failedBecause = failedBecause;
	}
	public boolean isMissingTags() {
		return missingTags != null;
	}

	public String getName() {
		return name;
	}
	
	public String[] getMissingTags() {
		return missingTags;
	}

	public String getFailedBecause() {
		return failedBecause;
	}
	
	public String[] getMatches() {
		return matches;
	}
	
	public boolean shouldStoreValues() {
		return shouldStoreValues;
	}

	
	public static FindResult success(String name, String[] matches, boolean shouldStoreValues) {
		return new FindResult(name, matches, shouldStoreValues);
	}
	
	public static FindResult missingTags(String[] missingTags) {
		return new FindResult(missingTags);
	}
	
	/**
	 * Obtain a {@link ScraperResult} with failure information.
	 * @param pattern
	 * @param minMatch
	 * @param maxMatch
	 * @param source
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static FindResult noMatchesFailure(Pattern pattern, int minMatch,
			int maxMatch, String source) {
		 return new FindResult("Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.quoteAndTruncate(StringUtils.quote(source), 100));
	}
}
