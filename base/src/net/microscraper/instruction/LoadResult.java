package net.microscraper.instruction;

import net.microscraper.client.ScraperResult;
import net.microscraper.http.HttpException;
import net.microscraper.template.HashtableSubstitutionOverwriteException;
import net.microscraper.util.Result;

public class LoadResult implements Result {
	private String url;
	private String responseBody;
	private String[] missingTags;
	private String failedBecause;
	
	private LoadResult(String url, String responseBody) {
		this.url = url;
		this.responseBody = responseBody;
	}
	
	private LoadResult(String[] missingTags) {
		this.missingTags = missingTags;
	}
	
	private LoadResult(String failedBecause) {
		this.failedBecause = failedBecause;
	}

	public String getUrl() {
		return url;
	}
	
	public String getResponseBody() {
		return responseBody;
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

	
	public static LoadResult success(String url, String responseBody) {
		return new LoadResult(url, responseBody);
	}
	/*
	public static LoadResult successWithoutBody() {
		return new LoadResult((Object) null); // force 'successful' constructor.
	}*/
	
	public static LoadResult missingTags(String[] missingTags) {
		return new LoadResult(missingTags);
	}
	
	/**
	 * Failed because of an HTTP exception.
	 * @param e A {@link HttpException} that caused this scrape to
	 * fail.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static LoadResult fromHttpException(HttpException e) {
		return new LoadResult("Failure during HTTP request or response: " + e.getMessage());
	}

	/**
	 * Failed because of substitution causing an ambiguous mapping.
	 * @param e A {@link HashtableSubstitutionOverwriteException} of the overwrite.
	 * @return A {@link ScraperResult} with failure information.
	 */
	public static LoadResult fromSubstitutionOverwrite(
			HashtableSubstitutionOverwriteException e) {
		return new LoadResult("Instruction template substitution caused ambiguous mapping: "
			+ e.getMessage());
	}
}
