package net.microscraper.resources;

import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.MissingVariable;
import net.microscraper.client.Variables;

/**
 * A throwable to indicate that while execution has failed, it should be retried later.
 * @author realest
 *
 */
public class ExecutionDelay extends Throwable implements ExecutionProblem {
	private final Throwable throwable;
	private final Executable executable;
	private final Variables variables;
	
	/**
	 * Generate an execution delay because of a Missing Variable in a Mustache template.
	 * @param missingVariable The MissingVariable throwable.
	 * @param executable The executable that was missing a variable.
	 * @param variables The variables in use (which did not contain the needed variable).
	 */
	public ExecutionDelay(MissingVariable missingVariable, Executable executable, Variables variables) {
		this.throwable = missingVariable;
		this.executable = executable;
		this.variables = variables;
	}
	
	/**
	 * Generate an execution delay because of a DelayRequest from a Browser.
	 * @param delayRequest The DelayRequest throwable.
	 * @param executable The executable that was missing a variable.
	 * @param variables The variables in use (which did not contain the needed variable).
	 */
	public ExecutionDelay(DelayRequest delayRequest, Executable executable, Variables variables) {
		this.throwable = delayRequest;
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
