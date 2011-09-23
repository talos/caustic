package net.microscraper.client;

import net.microscraper.instruction.InstructionResult;

/**
 * {@link Executor} implementations should be able to scrape
 * an array of {@link Scraper}s, and return their
 * {@link InstructionResult} results.
 * @author realest
 *
 */
public interface Executor {
	
	public void submit(Scraper[] scrapers);
	
	public void resubmit(Scraper scraper);
	
	public void join() throws InterruptedException;
	
	public void interrupt();
}
