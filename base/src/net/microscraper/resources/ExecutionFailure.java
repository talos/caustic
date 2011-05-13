package net.microscraper.resources;

import net.microscraper.client.Variables;

/**
 * A throwable to indicate that an execution has failed.  It should not be retried, but
 * other executions may continue.
 * @author realest
 *
 */
public class ExecutionFailure extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	private final Variables variables;
	public ExecutionFailure(Throwable throwable, Executable executable, Variables variables) {
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