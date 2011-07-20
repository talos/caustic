package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Scraper;
import net.microscraper.interfaces.browser.BrowserDelayException;

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
