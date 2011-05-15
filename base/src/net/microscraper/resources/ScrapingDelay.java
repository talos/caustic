package net.microscraper.resources;

import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.Problematic;

/**
 * A throwable to indicate that while execution has failed, it should be retried later.
 * @author realest
 *
 */
public class ScrapingDelay extends Throwable implements ScrapingProblem {
	private final Throwable throwable;
	private final Problematic executable;
	
	/**
	 * Generate an execution delay because of a Missing Variable in a Mustache template.
	 * @param missingVariable {@link MissingReference} The MissingReference throwable.
	 * @param executable The executable that was missing a variable.
	 */
	public ScrapingDelay(MissingReference missingVariable, Problematic executable) {
		this.throwable = missingVariable;
		this.executable = executable;
	}
	
	/**
	 * Generate an execution delay because of a DelayRequest from a Browser.
	 * @param delayRequest The DelayRequest throwable.
	 * @param executable The executable that was missing a variable.
	 */
	public ScrapingDelay(DelayRequest delayRequest, Problematic executable) {
		this.throwable = delayRequest;
		this.executable = executable;
	}
	public Problematic getExecutable() {
		return executable;
	}
	public Throwable getThrowable() {
		return throwable;
	}
}
