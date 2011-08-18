package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.regexp.Pattern;

/**
 * When successful, an {@link Execution} contains {@link String}s.  If the execution
 * failed because of a missing variable, it contains the missing variables.  If the
 * execution failed because of something else, it contains the reason why.
 * @author john
 *
 */
public final class Execution {
	
	private final String[] results;
	private final String[] missingVariables;
	private final String failedBecause;
	
	private Execution(String[] results, String[] missingVariables, String failure) {
		this.results = results;
		this.missingVariables = missingVariables;
		this.failedBecause = failure;
	}
	
	public static Execution success() {
		return new Execution(new String[] {}, null, null);
	}
	
	public static Execution success(String result) {
		return new Execution(new String[] { result }, null, null);
	}
	
	public static Execution success(String[] results) {
		return new Execution(results, null, null);
	}
	
	public static Execution missingVariables(String[] missingVariables) {
		return new Execution(null, missingVariables, null);
	}
	
	/*public static Execution failure(String failedBecause) {
		return new Execution(null, null, failedBecause);
	}*/
	public static Execution noMatches() {
		return new Execution(null, null, "No matches.");
	}
	
	public static Execution ioException(IOException e) {
		return new Execution(null, null, e.getMessage());
	}
	
	public static Execution failedTests(Pattern[] failedTests) {
		// TODO this is a missed opportunity!
		return new Execution(null, null, "failed tests");
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has failed, <code>false</code> otherwise.
	 * @see #run()
	 * @see #failedBecause()
	 */
	public boolean hasFailed() {
		return failedBecause == null ? false : true;
	}
	
	/**
	 * 
	 * @return What caused the {@link Execution} to fail.
	 * @see #run()
	 * @see #hasFailed()
	 */
	public String failedBecause() {
		if(!hasFailed())
			throw new IllegalStateException();
		return failedBecause;
	}

	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} was a success, <code>false</code> otherwise.
	 * @see #getResults()
	 */
	public boolean isComplete() {
		return results == null ? false : true;
	}
	
	/**
	 * 
	 * @return The {@link String}s of this {@link Execution}.
	 */
	public String[] getResults() {
		if(!isComplete())
			throw new IllegalStateException();
		return results;
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} is missing variables, <code>false</code> otherwise.
	 * @see #getMissingVariables()
	 */
	public boolean isMissingVariables() {
		return missingVariables == null ? false : true;
	}
	
	public String[] getMissingVariables() {
		if(!isMissingVariables()) 
			throw new IllegalStateException();
		return missingVariables;
	}
}
