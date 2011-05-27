package net.microscraper.client.executable;

import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.server.resource.Scraper;

/**
 * {@link SpawnedScraperExecutable} is the {@link Executable} spawned by a {@link ScraperExecutable}.
 * It works as a pass-through for its source result.
 * @author john
 *
 */
public class SpawnedScraperExecutable extends ScraperExecutable {	
	protected SpawnedScraperExecutable(Interfaces context,
				Scraper scraper, Variables variables, Result source) {
		super(context, scraper, variables, source);
	}

	/**
	 * Works as a pass-through for {@link #getSource()}.
	 */
	protected Result[] generateResults() throws BrowserDelayException,
			MissingVariableException, MustacheTemplateException,
			ExecutionFailure {
		return new Result[] { getSource() };
	}
}
