package net.caustic.instruction;

import net.caustic.deserializer.DeserializerResult;
import net.caustic.http.HttpException;
import net.caustic.regexp.Pattern;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.util.Result;
import net.caustic.util.StringUtils;

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

	/**
	 * Obtain a {@link InstructionResult} with failure information.
	 * @param pattern
	 * @param minMatch
	 * @param maxMatch
	 * @param source
	 * @return A {@link InstructionResult} with failure information.
	 */
	public static InstructionResult noMatchesFailure(Pattern pattern, int minMatch,
			int maxMatch, String source) {
		 return new InstructionResult("Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.quoteAndTruncate(StringUtils.quote(source), 100));
	}

	/**
	 * Failed because of an HTTP exception.
	 * @param e A {@link HttpException} that caused this scrape to
	 * fail.
	 * @return A {@link InstructionResult} with failure information.
	 */
	public static InstructionResult fromHttpException(HttpException e) {
		return new InstructionResult("Failure during HTTP request or response: " + e.getMessage());
	}

	/**
	 * Failed because of substitution causing an ambiguous mapping.
	 * @param e A {@link HashtableSubstitutionOverwriteException} of the overwrite.
	 * @return A {@link InstructionResult} with failure information.
	 */
	public static InstructionResult fromSubstitutionOverwrite(
			HashtableSubstitutionOverwriteException e) {
		return new InstructionResult("Instruction template substitution caused ambiguous mapping: "
			+ e.getMessage());
	}
	
	public static InstructionResult failed(DeserializerResult failedDeserializerResult) {
		return new InstructionResult(failedDeserializerResult.getFailedBecause());
	}
}
