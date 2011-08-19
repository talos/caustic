package net.microscraper.instruction;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.regexp.Pattern;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.Variables;

/**
 * When successful, an {@link Execution} contains {@link String}s.  If the execution
 * failed because of a missing variable, it contains the missing variables.  If the
 * execution failed because of something else, it contains the reason why.
 * @author john
 *
 */
public final class Execution {
	
	//private final String name;
	//private final String[] resultValues;
	private final Executable[] children;
	private final String[] missingVariables;
	private final String failedBecause;
	
	private Execution(Executable[] children, String[] missingVariables, String failure) {
		this.children = children;
		this.missingVariables = missingVariables;
		this.failedBecause = failure;
	}
	
	public static Execution success(Variables variables,
			String name, String[] resultValues, Instruction[] childInstructions) {
		Executable[] childExecutables = new Executable[resultValues.length * childInstructions.length];
		for(int i = 0 ; i < childInstructions.length ; i++) {
			
			// Only one resultValue, modifies the Variables (passes up
			// the new value).
			if(resultValues.length == 1) {
				variables.put(name, resultValues[0]);
				childExecutables[i] = childInstructions[i].bind(variables, resultValues[0]);
			
			// Multiple resultValues, copies but does not modify the Variables.
			} else {
				for(int j = 0 ; j < resultValues.length ; j++) {
					Variables branchedVariables = variables.branch(name, resultValues[j]);
					childExecutables[i * childInstructions.length + j] = childInstructions[i].bind(branchedVariables, resultValues[j]);
				}
			}
		}
		return new Execution(childExecutables, null, null);
	}
	
	public static Execution missingVariables(String[] missingVariables) {
		return new Execution(null, missingVariables, null);
	}
	
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
	public boolean isSuccessful() {
		return children == null ? false : true;
	}
	
	/**
	 * 
	 * @return The {@link Executable}s resulting from this successful {@link Execution}.
	 */
	public Executable[] generateChildren() {
		if(!isSuccessful())
			throw new IllegalStateException();
		return children;
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
