package net.microscraper.resources;

import net.microscraper.resources.definitions.Problematic;

/**
 * Interface to pass exception information from executable executions.
 * @author realest
 *
 */
public interface ScrapingProblem {
	/**
	 * 
	 * @return The {@link Problematic} that caused this problem.
	 */
	public abstract Problematic getExecutable();
	
	/**
	 * 
	 * @return The Throwable that made the {@link Problematic} issue the ScrapingProblem.
	 */
	public abstract Throwable getThrowable();
}