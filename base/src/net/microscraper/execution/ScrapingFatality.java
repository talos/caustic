package net.microscraper.execution;

import net.microscraper.model.Problematic;

/**
 * An ExceptionProblem interface to indicate that an execution cannot be completed, and that
 * no other executions should be attempted.
 * @author realest
 *
 */
public class ScrapingFatality extends Throwable implements ScrapingProblem {
	private final Throwable throwable;
	private final Problematic executable;
	public ScrapingFatality(Throwable throwable, Problematic executable) {
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
