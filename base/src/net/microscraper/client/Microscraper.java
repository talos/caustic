package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionRunner;
import net.microscraper.instruction.Load;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.uri.MalformedUriException;

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
	private final BasicLog log = new BasicLog();
	private final Deserializer deserializer;
	private final Database database;
	
	/**
	 * @param deserializer A {@link Deserializer} to use to instantiate {@link Instruction}s.
	 * @param database the {@link Database} to use for storage.
	 */
	public Microscraper(Deserializer deserializer, Database database) {
		this.deserializer = deserializer;
		this.database = database;
	}
	
	private void scrape(Instruction instruction, Hashtable[] defaultsHashes, String source)
			throws DeserializationException, IOException {
		for(int i = 0 ; i < defaultsHashes.length ; i ++) {
			InstructionRunner runner = new InstructionRunner(instruction, database, defaultsHashes[i], source);
			runner.register(log);
			runner.run();
		}
		database.clean();
		database.close();
	}

	private void scrapeFromJSON(String instructionJson, Hashtable[] defaultsHashes, String source)
			throws DeserializationException, IOException {
		scrape(deserializer.deserializeJson(instructionJson), defaultsHashes, source);
	}
	
	private void scrapeFromUri(String uri, Hashtable[] defaultsHashes, String source)
			throws DeserializationException, IOException {
		scrape(deserializer.deserializeUri(uri), defaultsHashes, source);
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param instructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 */
	public void scrapeFromJson(String instructionJSON)
			throws DeserializationException, IOException {
		scrapeFromJSON(instructionJSON, new Hashtable[] { new Hashtable() }, null);
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param instructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String instructionJSON, Hashtable defaults)
			throws DeserializationException, IOException, InterruptedException {
		scrapeFromJSON(instructionJSON, new Hashtable[] { defaults }, null );
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String for each member of <code>defaultsArray</code>.
	 * @param instructionJSONinstructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String instructionJSON, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, InterruptedException {
		scrapeFromJSON(instructionJSON, defaultsArray, null);
	}

	/**
	 * Scrape from a {@link Load} loaded from a URI.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 */
	public void scrapeFromUri(String uri)
			throws DeserializationException, IOException, InterruptedException,
			MalformedUriException  {
		scrapeFromUri(uri, new Hashtable[] { new Hashtable() }, null);
	}
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 */
	public void scrapeFromUri(String uri, Hashtable defaults)
			throws DeserializationException, IOException, InterruptedException, 
			MalformedUriException  {
		scrapeFromUri(uri, new Hashtable[] { defaults } , null);
	}

	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromUri(String uri, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, InterruptedException,
					MalformedUriException {
		scrapeFromUri(uri, defaultsArray, null);
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
