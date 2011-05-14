package net.microscraper.resources;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.resources.definitions.Executable;

/**
 * A throwable to indicate that an execution has failed.  It should not be retried, but
 * other executions may continue.
 * @author realest
 *
 */
public class ExecutionFailure extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	
	/**
	 * Throw an ExecutionFailure because there were no matches.
	 * @param throwable A NoMatches throwable.
	 * @param executable The running executable.
	 * @param variables A copy of the available variables.
	 */
	public ExecutionFailure(NoMatches throwable, Executable executable) {
		this.throwable = throwable;
		this.executable = executable;
	}

	/**
	 * Throw an ExecutionFailure because there was a Browser exception.
	 * @param throwable A BrowserException throwable.
	 * @param executable The running executable.
	 * @param variables A copy of the available variables.
	 */
	public ExecutionFailure(BrowserException throwable, Executable executable) {
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