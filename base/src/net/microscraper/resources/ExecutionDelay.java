package net.microscraper.resources;

import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.Executable;

/**
 * A throwable to indicate that while execution has failed, it should be retried later.
 * @author realest
 *
 */
public class ExecutionDelay extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	
	/**
	 * Generate an execution delay because of a Missing Variable in a Mustache template.
	 * @param missingVariable {@link MissingReference} The MissingReference throwable.
	 * @param executable The executable that was missing a variable.
	 */
	public ExecutionDelay(MissingReference missingVariable, Executable executable) {
		this.throwable = missingVariable;
		this.executable = executable;
	}
	
	/**
	 * Generate an execution delay because of a DelayRequest from a Browser.
	 * @param delayRequest The DelayRequest throwable.
	 * @param executable The executable that was missing a variable.
	 */
	public ExecutionDelay(DelayRequest delayRequest, Executable executable) {
		this.throwable = delayRequest;
		this.executable = executable;
	}
	public Executable getExecutable() {
		return executable;
	}
	public Throwable getThrowable() {
		return throwable;
	}
}
