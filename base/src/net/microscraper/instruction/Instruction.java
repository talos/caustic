package net.microscraper.instruction;

import java.util.Hashtable;

import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;

/**
 * {@link Instruction}s can be scraped using {@link #execute(String, Hashtable)}
 * to generate {@link ScraperResult}s.
 * @author talos
 *
 */
public interface Instruction {
	
	/**
	 * 
	 * @param source The {@link String} to use as the source
	 * for this {@link Instruction}.
	 * @param view The {@link DatabaseView} to use as input
	 * for template substitutions.
	 * @return A {@link ScraperResult} object with either successful
	 * values and children, or information about why
	 * this method did not work.
	 * @throws InterruptedException if the user interrupted during
	 * the method.
	 * @throws DatabaseException if there was an error persisting to 
	 * or reading from <code>input</code>.
	 */
	public ScraperResult execute(String source, DatabaseView view)
			throws InterruptedException, DatabaseException;
}
