package net.microscraper.resources;

import net.microscraper.client.Variables;

/**
 * Interface to pass exception information from executable executions.
 * @author realest
 *
 */
public interface ExecutionProblem {
	/**
	 * 
	 * @return The Executable whose execution caused this problem.
	 */
	public abstract Executable getExecutable();
	
	/**
	 * 
	 * @return The Throwable that made the executable issue the ExecutionProblem.
	 */
	public abstract Throwable getThrowable();
	
	/**
	 * 
	 * @return The Variables in use when the executable issued the ExecutionProblem.
	 */
	public abstract Variables getVariables();
}
