package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Scraper;

/**
 * {@link ScraperExecutableChild} is a {@link ScraperExecutable} subclass spawned
 * by a {@link ScraperExecutable} (including other {@link ScraperExecutableChild}ren
 *  or a {@link FindManyExecutable}.
 * @see ScraperExecutable
 * @see FindManyExecutable
 * @author john
 *
 */
public final class ScraperExecutableChild extends ScraperExecutable {
	private final String spawnedWithName;
	
	/**
	 * If a {@link ScraperExecutableChild} was spawned from a {@link FindManyExecutable},
	 * it will have a particular value it can feed from instead of from a {@link PageExecutable}.
	 */
	private final String spawnedWithValue;
	
	public ScraperExecutableChild(Interfaces context, 
			Executable parent, Scraper scraper, Variables variables) {
		super(context, parent, scraper, variables);
		this.spawnedWithName = null;
		this.spawnedWithValue = null;
	}
	public ScraperExecutableChild(Interfaces context, 
			Executable parent, Scraper scraper, Variables variables,
			String spawnedWithName, String spawnedWithValue) {
		super(context, parent, scraper, variables);
		this.spawnedWithName = spawnedWithName;
		this.spawnedWithValue = spawnedWithValue;
	}
	

	/**
	 * For {@link ScraperExecutableChild}, it is possible to execute without an explicit source,
	 * provided {@link #spawnedWithValue} was set.
	 */
	protected Object generateResult()
				throws MissingVariableException, BrowserDelayException,
				ExecutionFailure, MustacheTemplateException {
		Scraper scraper = (Scraper) getResource();
		if(!scraper.hasSource && spawnedWithValue != null) {
			return spawnedWithValue;
		}
		return super.generateResult();
	}
}
