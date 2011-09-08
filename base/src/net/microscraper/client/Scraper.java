package net.microscraper.client;

import java.util.Vector;

import net.microscraper.instruction.Instruction;
import net.microscraper.util.StringMap;
import net.microscraper.util.VectorUtils;

/**
 * The {@link Scraper} class can be used to scrape an {@link Instruction} with
 * an input {@link StringMap} and {@link String} source.<p>
 * @author realest
 *
 */
public class Scraper {
	
	private final Instruction instruction;
	private final StringMap input;
	private final String source;
	
	private ScraperResult curResult;
	private ScraperResult lastResult;
	
	/**
	 * Create a new {@link Scraper}.
	 * @param instruction The {@link Instruction} to execute when scraping.
	 * @param input The {@link StringMap} to use as input when scraping.
	 * @param source The {@link String} to use as a source when scraping.
	 * @see #scrape()
	 */
	public Scraper(Instruction instruction, StringMap input, String source)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
	}
	
	/**
	 * Try to scrape.  If {@link ScraperResult} is missing tags, it is recommended
	 * to retry unless {@link #isStuck()} is <code>true</code> and no further modifications
	 * to the backing {@link #input} are possible.
	 * @return A {@link ScraperResult} with the results of this {@link Scraper},
	 * or information about why it didn't work.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 */
	public ScraperResult scrape() throws InterruptedException {
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
}
