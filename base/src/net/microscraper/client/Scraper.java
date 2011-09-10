package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.DatabaseView;
import net.microscraper.database.HashtableDatabaseView;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.util.StringUtils;
import net.microscraper.util.VectorUtils;

/**
 * The {@link Scraper} class can be used to scrape an {@link Instruction} with
 * an input {@link DatabaseView} and {@link String} source.<p>
 * @author realest
 *
 */
public class Scraper {
	
	private static final int SOURCE_TOSTRING_TRUNCATE = 100;
	
	private final Instruction instruction;
	private final DatabaseView input;
	private final String source;
	
	private ScraperResult curResult;
	private ScraperResult lastResult;

	/**
	 * Create a new {@link Scraper} without a source.  This is only possible
	 * for {@link Load}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link DatabaseView} to use as input when scraping.
	 * @see #scrape()
	 */
	public Scraper(Load load, DatabaseView input)  {
		this.instruction = load;
		this.input = input;
		this.source = null;
	}

	/**
	 * Create a new {@link Scraper} without a source.  This is only possible
	 * for {@link Load}.  Also uses a {@link Hashtable} with string-string
	 * mappings as input.  This will be converted into a {@link HashtableDatabaseView}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link Hashtable} to use as input when scraping.  This will
	 * be converted into a {@link HashtableDatabaseView}.
	 * @see #scrape()
	 */
	public Scraper(Load load, Hashtable input)  {
		this.instruction = load;
		this.input = new HashtableDatabaseView(input);
		this.source = null;
	}
	
	/**
	 * Create a new {@link Scraper}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link DatabaseView} to use as input when scraping.
	 * @param source The {@link String} to use as a source when scraping.
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, DatabaseView input, String source)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
	}

	/**
	 * Create a new {@link Scraper} using a {@link Hashtable} with string-string
	 * mappings as input.  This will be converted into a {@link HashtableDatabaseView}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link Hashtable} to use as input when scraping.  This will
	 * be converted into a {@link HashtableDatabaseView}.
	 * @param source The {@link String} to use as a source when scraping.
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, Hashtable input, String source)  {
		this.instruction = instruction;
		this.input = new HashtableDatabaseView(input);
		this.source = source;
	}
	
	/**
	 * Try to scrape.  If {@link ScraperResult} is missing tags, it is recommended
	 * to retry unless {@link #isStuck()} is <code>true</code> and no further modifications
	 * to the backing {@link #input} are possible.
	 * @return A {@link ScraperResult} with the results of this {@link Scraper},
	 * or information about why it didn't work.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 * @throws IOException If there was an error persisting to the {@link DatabaseView}.
	 */
	public ScraperResult scrape() throws InterruptedException, IOException {
		lastResult = curResult;
		curResult = instruction.execute(source, input);
		return curResult;
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
				if(curMissingTags.length != lastMissingTags.length) {
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
		return "Scraper running " + StringUtils.quote(instruction.getClass()) + " " +
				StringUtils.quote(instruction.toString()) + " with tags substituted from " +
				StringUtils.quote(input.toString()) +
				(source == null ?
						"" :
						" and source " + StringUtils.quote(StringUtils.quoteAndTruncate(source, SOURCE_TOSTRING_TRUNCATE)));
	}
}
