package net.microscraper.resources;

import net.microscraper.client.Variables;

/**
 * An ExceptionProblem interface to indicate that an execution cannot be completed, and that
 * no other executions should be attempted.
 * @author realest
 *
 */
public class ExecutionFatality extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	private final Variables variables;
	public ExecutionFatality(Throwable throwable, Executable executable, Variables variables) {
		this.throwable = throwable;
		this.executable = executable;
		this.variables = variables;
	}
	
	public Executable getExecutable() {
		return executable;
	}

	public Throwable getThrowable() {
		return throwable;
	}
	
	public Variables getVariables() {
		return variables;
	}
}
