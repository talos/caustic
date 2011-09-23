package net.microscraper.client;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.util.StringUtils;
import net.microscraper.util.VectorUtils;

/**
 * The {@link Scraper} class can be used to repeatedly scrape an {@link Instruction} with
 * an input {@link DatabaseView} and {@link String} source.<p>
 * This makes it easier to retry when tags are missing, and keep track of 
 * (if {@link #isStuck()} is <code>true</code> when a retry should be delayed.
 * @author realest
 *
 */
public class Scraper {
	
	private final Instruction instruction;
	private final DatabaseView input;
	private final Executor executor;
	private HttpBrowser browser;
	private String source;
	
	private InstructionResult instructionResult;
	private Scraper[] children;
	private String[] missingTags;
	private String[] lastMissingTags;
	
	private void populateChildren() throws DatabaseException {
		final String name = instructionResult.getName();
		final String[] results = instructionResult.getResults();
		final boolean shouldStoreValues = instructionResult.shouldStoreValues();
		final Instruction[] childInstructions = instructionResult.getChildren();
		
		// create children.
		children = new Scraper[childInstructions.length * results.length];
		//final DatabaseView[] childViews = new DatabaseView[results.length];
		for(int i = 0 ; i < results.length ; i ++) {
			final DatabaseView childView;
			// generate result views.
			final String resultValue = results[i];
			if(results.length == 1) { // don't spawn a new result for single match
				childView = input;
				if(shouldStoreValues) {
					input.put(name, resultValue);
				}
			} else {
				if(shouldStoreValues) {
					childView = input.spawnChild(name, resultValue);
				} else {
					childView = input.spawnChild(name);							
				}
			}
			for(int j = 0 ; j < childInstructions.length ; j ++) {
				children[i * childInstructions.length + j] =
					new Scraper(childInstructions[j], childView, results[i],
						browser.copy(), executor);
			}
		}
	}
	
	private Scraper(Instruction instruction, DatabaseView input, String source, HttpBrowser browser,
			Executor executor)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
		this.browser = browser;
		this.executor = executor;
	}
	
	/**
	 * Create a new {@link Scraper}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link DatabaseView} to use as input when scraping.
	 * @param source The {@link String} to use as a source when scraping.
	 * @param browser
	 * @param threads
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, DatabaseView input, String source, HttpBrowser browser,
			int threads)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
		this.browser = browser;
		this.executor = new AsyncExecutor(threads);
	}

	/**
	 * Create a new {@link Scraper} using a {@link Hashtable} with string-string
	 * mappings as input.  This will be converted into a {@link InMemoryDatabaseView}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link Hashtable} to use as input when scraping.  This will
	 * be converted into a {@link InMemoryDatabaseView}.
	 * @param source The {@link String} to use as a source when scraping.
	 * @param browser
	 * @param threads
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, Hashtable input, String source, HttpBrowser browser,
			int threads)  {
		this.instruction = instruction;
		this.input = new InMemoryDatabaseView(input);
		this.source = source;
		this.browser = browser;
		this.executor = new AsyncExecutor(threads);
	}

	/**
	 * Scrape using this {@link Scraper}'s assigned {@link Instruction}.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 * @throws DatabaseException If there was an error persisting to or reading
	 * from the {@link DatabaseView}.
	 */
	public void scrape() throws InterruptedException, DatabaseException {		

		lastMissingTags = missingTags;
		
		// attempt to scrape
		if(instructionResult == null) {
			instructionResult = instruction.execute(source, input, browser);
		} else if(instructionResult.isMissingTags()) {
			instructionResult = instruction.execute(source, input, browser);
		}
		
		// we won't be running this particular instruction again, save some memory.
		if(!instructionResult.isMissingTags()) {
			source = null;
		}
		
		if(instructionResult.isSuccess()) {
			// create children if they haven't yet been created
			if(children == null) {
				populateChildren();	
				browser = null; // can clear out browser at this point
			}
			
			// scrape children using executor
			executor.submit(children);
		} else if(instructionResult.isMissingTags()) {
			missingTags = instructionResult.getMissingTags();
			executor.resubmit(this);
		}
	}
	
	/**
	 * Determine whether this {@link Scraper} is stuck.
	 * @return <code>true</code> if, in two consecutive runs, this {@link Scraper}
	 * has generated a {@link ScraperResult} missing identical sets of tags.
	 */
	public boolean isStuck() {
		if(missingTags != null && lastMissingTags != null) {
			if(missingTags.length == lastMissingTags.length) { // only bother testing if the same length
				Vector curVector = VectorUtils.arrayIntoVector(missingTags, new Vector());
				Vector lastVector = VectorUtils.arrayIntoVector(lastMissingTags, new Vector());
				return VectorUtils.haveSameElements(curVector, lastVector);
			}
		}
		return false;
	}
	
	/**
	 * Returns a {@link String} containing information about the {@link Instruction}
	 * this {@link Scraper} executes, in addition to the state of its {@link DatabaseView}
	 * and source, if any.
	 */
	public String toString() {
		return  StringUtils.simpleClassName(instruction) + " " +
				StringUtils.quote(instruction.toString()) + " with tags substituted from " +
				StringUtils.quote(input.toString());
	}
	
	public void join() throws InterruptedException {
		executor.join();
	}

	public void interrupt() {
		executor.interrupt();
	}
}
