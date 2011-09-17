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
	
	private static final int SOURCE_TOSTRING_TRUNCATE = 100;
	
	private final Instruction instruction;
	private final DatabaseView input;
	private String source;
	private final HttpBrowser browser;
	
	private ScraperResult curResult;
	private ScraperResult lastResult;

	/**
	 * Create a new {@link Scraper}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link DatabaseView} to use as input when scraping.
	 * @param source The {@link String} to use as a source when scraping.
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, DatabaseView input, String source, HttpBrowser browser)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
		this.browser = browser;
	}

	/**
	 * Create a new {@link Scraper} using a {@link Hashtable} with string-string
	 * mappings as input.  This will be converted into a {@link InMemoryDatabaseView}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link Hashtable} to use as input when scraping.  This will
	 * be converted into a {@link InMemoryDatabaseView}.
	 * @param source The {@link String} to use as a source when scraping.
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, Hashtable input, String source, HttpBrowser browser)  {
		this.instruction = instruction;
		this.input = new InMemoryDatabaseView(input);
		this.source = source;
		this.browser = browser;
	}
	
	/**
	 * Try to scrape.  If {@link ScraperResult} is missing tags, it is recommended
	 * to retry unless {@link #isStuck()} is <code>true</code> and no further modifications
	 * to the backing {@link #input} are possible.
	 * @return A {@link ScraperResult} with the results of this {@link Scraper},
	 * or information about why it didn't work.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 * @throws DatabaseException If there was an error persisting to the {@link DatabaseView}.
	 */
	public ScraperResult scrape() throws InterruptedException, DatabaseException {
		lastResult = curResult;
		
		InstructionResult instructionResult = instruction.execute(source, input, browser);
		if(instructionResult.isSuccess()) {
			source = null; // this could be quite a large hunk of String, want to clean it up ASAP.
			
			final String name = instructionResult.getName();
			final String[] results = instructionResult.getResults();
			final boolean shouldStoreValues = instructionResult.shouldStoreValues();
			final Instruction[] children = instructionResult.getChildren();
			
			// handle database storage
			final DatabaseView[] childViews = new DatabaseView[results.length];
			for(int i = 0 ; i < results.length ; i ++) {
				
				// generate result views.
				final String resultValue = results[i];
				if(results.length == 1) { // don't spawn a new result for single match
					childViews[i] = input;
					if(shouldStoreValues) {
						input.put(name, resultValue);
					}
				} else {
					if(shouldStoreValues) {
						childViews[i] = input.spawnChild(name, resultValue);
					} else {
						childViews[i] = input.spawnChild(name);							
					}
				}
			}

			// handle creation of children.
			Scraper[] scraperChildren = new Scraper[children.length * results.length];
			for(int i = 0 ; i < children.length ; i ++) {
				for(int j = 0 ; j < results.length ; j ++) {
					scraperChildren[i * results.length + j] =
							new Scraper(children[i], childViews[j], results[j], browser.copy());
				}
			}
			curResult = ScraperResult.success(name, childViews, scraperChildren);
		} else if(instructionResult.isMissingTags()) {
			curResult = ScraperResult.missingTags(instructionResult.getMissingTags(), this);
		} else {
			curResult = ScraperResult.failed(instructionResult);
		}
		
		return curResult;
	}
	

	/**
	 * Try to scrape.  If {@link ScraperResult} is missing tags, it is recommended
	 * to retry unless {@link #isStuck()} is <code>true</code> and no further modifications
	 * to the backing {@link #input} are possible.
	 * @return A {@link ScraperResult} with the results of this {@link Scraper},
	 * or information about why it didn't work.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 * @throws DatabaseException If there was an error persisting to the {@link DatabaseView}.
	 */
	public ScraperResult[] scrapeAll() throws InterruptedException, DatabaseException {
		Vector scraperResults = new Vector();
		ScraperResult initialResult = scrape();
		if(initialResult.isMissingTags() || initialResult.getFailedBecause() != null) {
			return new ScraperResult[] { initialResult }; // fail out
		} else {
			scraperResults.add(initialResult);
			Vector scrapersToScrape = new Vector();
			
			VectorUtils.arrayIntoVector(initialResult.getChildren(), scrapersToScrape);
			
			// TODO this infinite-loops over stuck tags.
			throw new RuntimeException("unsupported");
			
			/*while(scrapersToScrape.size() > 0) {
				Scraper scraper = (Scraper) scrapersToScrape.elementAt(0);
				scrapersToScrape.removeElementAt(0);
				ScraperResult result = scraper.scrape();
				if(result.isMissingTags()) {
					scrapersToScrape.add(scraper);
				} else if(result.getChildren() != null) {
					scraperResults.add(result);
					VectorUtils.arrayIntoVector(result.getChildren(), scrapersToScrape);
				} else {
					scraperResults.add(result);
				}
			}
			
			ScraperResult[] scraperResultsAry = new ScraperResult[scraperResults.size()];
			scraperResults.copyInto(scraperResultsAry);
			return scraperResultsAry;*/
		}
	}
	
	/**
	 * Determine whether this {@link Scraper} is stuck.
	 * @return <code>true</code> if, in two consecutive runs, this {@link Scraper}
	 * has generated a {@link ScraperResult} missing identical sets of tags.
	 */
	public boolean isStuck() {
		if(curResult != null && lastResult != null) {
			if(curResult.isMissingTags() && lastResult.isMissingTags()) {
				String[] curMissingTags = curResult.getMissingTags();
				String[] lastMissingTags = lastResult.getMissingTags();
				if(curMissingTags.length == lastMissingTags.length) { // only bother testing if the same length
					Vector curVector = VectorUtils.arrayIntoVector(curMissingTags, new Vector());
					Vector lastVector = VectorUtils.arrayIntoVector(lastMissingTags, new Vector());
					return VectorUtils.haveSameElements(curVector, lastVector);
				}
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
				StringUtils.quote(input.toString()) +
				(source == null ?
						"" :
						" and source " + StringUtils.quote(StringUtils.quoteAndTruncate(source, SOURCE_TOSTRING_TRUNCATE)));
	}
}
