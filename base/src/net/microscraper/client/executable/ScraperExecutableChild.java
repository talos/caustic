package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.NameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
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
	/**
	 * If a {@link ScraperExecutableChild} was spawned from a {@link FindManyExecutable},
	 * it will have a particular value it can feed from instead of from a {@link PageExecution}.
	 */
	private final NameValuePair spawnedWithResult;
	
	public ScraperExecutableChild(Interfaces context, 
			Executable parent, Scraper scraper, Variables variables) {
		super(context, parent, scraper, variables);
		this.spawnedWithResult = null;
	}
	public ScraperExecutableChild(Interfaces context, 
			Executable parent, Scraper scraper, Variables variables,
			NameValuePair spawnedWithResult) {
		super(context, parent, scraper, variables);
		this.spawnedWithResult = spawnedWithResult;
	}
	

	/**
	 * For {@link ScraperExecutableChild}, it is possible to execute without an explicit source,
	 * provided {@link #spawnedWithValue} was set.
	 */
	protected NameValuePair[] generateResults()
				throws MissingVariableException, BrowserDelayException,
				ExecutionFailure, MustacheTemplateException {
		Scraper scraper = (Scraper) getResource();
		if(!scraper.hasSource && spawnedWithResult != null) {
			return new NameValuePair[0];
		}
		return super.generateResults();
	}
	
	protected Executable[] generateChildren(NameValuePair[] result) {
		if(spawnedWithResult != null) {
			return generateChildren(spawnedWithResult.getValue());
		}
		return super.generateChildren(result);
	}
}
