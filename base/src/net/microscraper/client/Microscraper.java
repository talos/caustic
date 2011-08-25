package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.instruction.InstructionRunner;
import net.microscraper.instruction.Load;
import net.microscraper.template.Template;

/**
 * A {@link Microscraper} can scrape an {@link Instruction}.
 * @author john
 * @see #scrapeWithJSON(String)
 * @see #scrapeWithJSON(String, String)
 * @see #scrapeWithJSON(String, String, Hashtable)
 * @see #scrapeWithURI(String)
 * @see #scrapeWithURI(String, String)
 * @see #scrapeWithURI(String, String, Hashtable)
 *
 */
public class Microscraper implements Loggable {	
	private final String executionDir;
	private final BasicLog log = new BasicLog();
	private final Deserializer deserializer;
	private final Database database;
	
	/**
	 * @param deserializer A {@link Deserializer} to use to instantiate {@link Instruction}s.
	 * @param database the {@link Database} to use for storage.
	 * @param executionDir the {@link String} path to where {@link Microscraper}
	 * is being executed from. This is used to resolve the path to local instructions.
	 */
	public Microscraper(Deserializer deserializer, Database database, String executionDir) {
		this.deserializer = deserializer;
		this.database = database;
		this.executionDir = executionDir;
	}
	
	private void scrape(String instructionString, Hashtable[] defaultsHashes, String source) throws IOException {
		InstructionPromise promise = new InstructionPromise(deserializer, instructionString, executionDir);
		for(int i = 0 ; i < defaultsHashes.length ; i ++) {
			InstructionRunner runner = new InstructionRunner(promise, database, defaultsHashes[i], source);
			runner.register(log);
			runner.run();
		}
		database.close();
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 */
	public void scrape(String serializedString) throws IOException {
		scrape(serializedString, new Hashtable[] { new Hashtable() }, null);
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>serializedString</code> {@link Template} tags.
	 */
	public void scrape(String serializedString, Hashtable defaults) throws IOException  {
		scrape(serializedString, new Hashtable[] { defaults }, null );
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>serializedString</code> {@link Template} tags.
	 */
	public void scrape(String serializedString, Hashtable[] defaultsArray) throws IOException  {
		scrape(serializedString, defaultsArray, null);
	}

	/**
	 * Set the rate limit for loading from a single host. Microscraper will wait until the rate is below this
	 * threshold before making another request.
	 * Set this to 0 to disable rate limiting.
	 * @param rateLimitKBPS The rate limit to use.
	 * @see Browser#setRateLimit(int)
	 */
	/*public void setRateLimit(int rateLimitKBPS) {
		browser.setRateLimit(rateLimitKBPS);
	}*/

	/**
	 * @param timeout How many seconds before giving up on a request.
	 * @see Browser#setTimeout(int)
	 */
	/*public void setTimeout(int timeout) {
		browser.setTimeout(timeout);
	}*/

	/**
	 * @param maxResponseSizeKB The maximum size of a single response in kilobytes that Microscraper will
	 * load before terminating. Since responses are fed straight through to a regex parser,
	 * it is wise not to deal with huge pages.
	 * @see Browser#setMaxResponseSize(int)
	 */
	/*public void setMaxResponseSize(int maxResponseSizeKB) {
		browser.setMaxResponseSize(maxResponseSizeKB);
	}*/

	public void register(Logger logger) {
		log.register(logger);
	}
}
