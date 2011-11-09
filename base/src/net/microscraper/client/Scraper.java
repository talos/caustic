package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.concurrent.AsyncExecutor;
import net.microscraper.concurrent.Executable;
import net.microscraper.concurrent.SyncExecutor;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.DatabaseViewHook;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;

/**
 * The {@link Scraper} class can be used to asynchronously or synchronously scrape
 * an {@link Instruction} and all its children using defined input and {@link String} source.<p>
 * Use the {@link DatabaseViewHook} to observe results as they are generated.
 * @author realest
 *
 */
public class Scraper {
	private final DatabaseView view;
	private final Executable executable;
	
	/**
	 * Create a new {@link Scraper}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param view The {@link DatabaseView} to use as input when scraping.
	 * @param source The {@link String} to use as a source when scraping.
	 * @param browser
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, DatabaseView view, String source, HttpBrowser browser)  {
		this.view = view;
		this.executable = new Executable(instruction, view, source, browser);
	}

	/**
	 * Create a new {@link Scraper} using a {@link Hashtable} with string-string
	 * mappings as input.  This will be converted into a {@link InMemoryDatabaseView}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link Hashtable} to use as input when scraping.  This will
	 * be converted into a {@link InMemoryDatabaseView}.
	 * @param source The {@link String} to use as a source when scraping.
	 * @param browser
	 * @param hook
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, Hashtable input, String source, HttpBrowser browser)  {
		this.view = new InMemoryDatabaseView(input);
		this.executable = new Executable(instruction, view, source, browser);
	}
	
	/**
	 * Scrape asynchronously using this {@link Scraper}'s assigned {@link Instruction} and
	 * a fixed number of threads.
	 * @param nThreads How many threads to use when scraping.  Must be 1 or greater.
	 * @return An {@link AsyncExecutor} that can be interrupted or joined to.
	 * @throws DatabaseException If there was an error generating {@link DatabaseView}.
	 */
	public AsyncExecutor scrape(int nThreads) throws DatabaseException {		
		AsyncExecutor executor = new AsyncExecutor(nThreads, executable);
		executor.start();
		return executor;
	}
	
	/**
	 * Scrape synchronously using this {@link Scraper}'s assigned {@link Instruction}.
	 * @throws DatabaseException If there was an error generating {@link DatabaseView}.
	 * @throws InterruptedException If the user interrupts the scraping.
	 */
	public void scrapeSync() throws DatabaseException, InterruptedException {
		SyncExecutor executor = new SyncExecutor(executable);
		executor.execute();
	}
	
	/**
	 * 
	 * @param hook A {@link DatabaseViewHook} to add to this {@link Scraper}'s 
	 * {@link DatabaseView}.
	 */
	public void addHook(DatabaseViewHook hook) {
		view.addHook(hook);
	}
}
