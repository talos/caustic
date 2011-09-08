package net.microscraper.instruction;

import net.microscraper.util.Result;

public class FindResult extends Result {

	private FindResult(Object matches) {
		super(matches);
	}

	private FindResult(String[] missingTags) {
		super(missingTags);
	}

	private FindResult(String failedBecause) {
		super(failedBecause);
	}
	
	public String[] getMatches() {
		return (String[]) getSuccess();
	}
	
	public static FindResult success(String[] matches) {
		return new FindResult((Object) matches);
	}
	
	public static FindResult missingTags(String[] missingTags) {
		return new FindResult(missingTags);
	}
	
	public static FindResult failed(String failedBecause) {
		return new FindResult(failedBecause);
	}
}
