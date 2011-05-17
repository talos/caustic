package net.microscraper.execution;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.model.Problematic;

/**
 * A throwable to indicate that an execution has failed.  It should not be retried, but
 * other executions may continue.
 * @author realest
 *
 */
public class ScrapingFailure extends Throwable implements ScrapingProblem {
	private final Throwable throwable;
	private final Problematic executable;
	
	/**
	 * Throw an ScrapingFailure because there were no matches.
	 * @param throwable A NoMatches throwable.
	 * @param executable The running executable.
	 * @param variables A copy of the available variables.
	 */
	public ScrapingFailure(NoMatches throwable, Problematic executable) {
		this.throwable = throwable;
		this.executable = executable;
	}

	/**
	 * Throw an ScrapingFailure because there was a Browser exception.
	 * @param throwable A BrowserException throwable.
	 * @param executable The running executable.
	 * @param variables A copy of the available variables.
	 */
	public ScrapingFailure(BrowserException throwable, Problematic executable) {
		this.throwable = throwable;
		this.executable = executable;
	}
	
	public Problematic getExecutable() {
		return executable;
	}
	public Throwable getThrowable() {
		return throwable;
	}
}