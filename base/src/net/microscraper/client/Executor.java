package net.microscraper.client;

import net.microscraper.database.DatabaseException;
import net.microscraper.instruction.InstructionResult;

/**
 * {@link Executor} implementations should be able to 
 * @author realest
 *
 */
public interface Executor {
	public InstructionResult[] scrape(Scraper[] scrapers) throws InterruptedException, DatabaseException;
}
