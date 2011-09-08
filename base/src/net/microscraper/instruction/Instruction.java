package net.microscraper.instruction;

import java.util.Hashtable;

import net.microscraper.client.ScraperResult;
import net.microscraper.util.StringMap;

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
	 * @param input The {@link StringMap} to use as input
	 * for template substitutions.
	 * @return A {@link ScraperResult} object with either successful
	 * values and children, or information about why
	 * {@link #execute(String, Hashtable)} did not work.
	 * @throws InterruptedException
	 */
	public ScraperResult execute(String source, StringMap input) throws InterruptedException;
}
