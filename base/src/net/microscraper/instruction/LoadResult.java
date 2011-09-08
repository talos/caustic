package net.microscraper.instruction;

import net.microscraper.util.Result;

public class LoadResult extends Result {

	private LoadResult(Object responseBody) {
		super(responseBody);
	}

	private LoadResult(String[] missingTags) {
		super(missingTags);
	}

	private LoadResult(String failedBecause) {
		super(failedBecause);
	}
	
	public String[] getMatches() {
		return (String[]) getSuccess();
	}
	
	public String getResponseBody() {
		return (String) getSuccess();
	}

	public static LoadResult success(String responseBody) {
		return new LoadResult((Object) responseBody);
	}
	
	public static LoadResult missingTags(String[] missingTags) {
		return new LoadResult(missingTags);
	}
	
	public static LoadResult failed(String failedBecause) {
		return new LoadResult(failedBecause);
	}
}
