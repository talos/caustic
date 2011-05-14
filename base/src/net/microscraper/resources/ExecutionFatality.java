package net.microscraper.resources;

import net.microscraper.resources.definitions.Executable;

/**
 * An ExceptionProblem interface to indicate that an execution cannot be completed, and that
 * no other executions should be attempted.
 * @author realest
 *
 */
public class ExecutionFatality extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	public ExecutionFatality(Throwable throwable, Executable executable) {
		this.throwable = throwable;
		this.executable = executable;
	}
	
	public Executable getExecutable() {
		return executable;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
