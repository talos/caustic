package net.microscraper.instruction;

import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.util.Result;

public class InstructionResult implements Result {
	private String[] missingTags;
	private String failedBecause;
	private Instruction[] children;
	private String name;
	private String[] results;
	private boolean shouldStoreResults;
	private boolean isSuccess = false;
	
	private InstructionResult(String name, String[] results, Instruction[] children,
			boolean shouldStoreResults) {
		this.isSuccess = true;
		this.name = name;
		this.results = results;
		this.children = children;
		this.shouldStoreResults = shouldStoreResults;
	}
	
	private InstructionResult(String[] missingTags) {
		this.missingTags = missingTags;
	}
	
	private InstructionResult(String failedBecause) {
		this.failedBecause = failedBecause;
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
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getName() {
		return name;
	}
	
	public Instruction[] getChildren() {
		return children;
	}
	
	public String[] getResults() {
		return results;
	}
	
	public boolean shouldStoreValues() {
		return shouldStoreResults;
	}
	
	public static InstructionResult success(String name, String[] results,
			Instruction[] children, boolean shouldStoreResults) {
		return new InstructionResult(name, results, children, shouldStoreResults);
	}
	
	public static InstructionResult missingTags(String[] missingTags) {
		return new InstructionResult(missingTags);
	}
	
	public static InstructionResult failed(FindResult failedFindResult) {
		return new InstructionResult(failedFindResult.getFailedBecause());
	}

	public static InstructionResult failed(LoadResult failedLoadResult) {
		return new InstructionResult(failedLoadResult.getFailedBecause());
	}

	public static InstructionResult failed(DeserializerResult failedDeserializerResult) {
		return new InstructionResult(failedDeserializerResult.getFailedBecause());
	}
}
