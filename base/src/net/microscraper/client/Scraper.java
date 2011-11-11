package net.microscraper.client;

import net.microscraper.concurrent.Executor;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;

/**
 * The {@link Scraper} class can be used to asynchronously or synchronously scrape
 * an {@link Instruction} and all its children using defined input and {@link String} source.<p>
 * Hook into the {@link DatabaseView} to observe results.
 * @author realest
 *
 */
public class Scraper {
	
	private final DatabaseView view;
	private final HttpBrowser browser;
	private final Executor executor;
	
	public Scraper(DatabaseView view, HttpBrowser browser, Executor executor)  {
		this.view = view;
		this.browser = browser;
		this.executor = executor;
	}
	
	public void scrape(String source, Instruction instruction)
				throws DatabaseException, InterruptedException {
		executor.execute(instruction, view, source, browser);		
	}
	
	public void scrape(Instruction instruction)
				throws DatabaseException, InterruptedException {
		executor.execute(instruction, view, null, browser);		
	}
}
