package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Page;
import net.microscraper.json.JSONParser;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.URIFactory;
import net.microscraper.uri.URIInterface;
import net.microscraper.util.BasicVariables;

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
	private final String userDir;
	private final RegexpCompiler compiler;
	private final Browser browser;
	private final URIFactory uriFactory;
	private final Database database;
	private final JSONParser jsonInterface;
	private final BasicLog log = new BasicLog();
	
	/**
	 * @param compiler The {@link RegexpCompiler} to use when compiling regular
	 * expressions.
	 * @param browser A {@link Browser} to use for HTTP requests.
	 * @param jsonInterface A {@link JSONParser} to use in parsing and loading JSON.
	 * @param uriFactory A {@link URIFactory} to create {@link URIInterface}s with.
	 * @param database the {@link Database} to use for storage.
	 */
	public Microscraper(RegexpCompiler compiler, Browser browser, URIFactory uriFactory,
			JSONParser jsonInterface, Database database) {
		this.compiler = compiler;
		this.browser = browser;
		this.jsonInterface = jsonInterface;
		this.database = database;
		this.uriFactory = uriFactory;
		this.userDir = System.getProperty("user.dir");
		this.browser.register(log);
	}
	
	private void scrape(JSONObjectInterface pageJson, Hashtable[] defaultsHash)
			throws DeserializationException, IOException, DatabaseException {
		for(int i = 0 ; i < defaultsHash.length ; i ++) {
			new Page(pageJson).execute(compiler, browser, BasicVariables.fromHashtable(defaultsHash[i]), null, database, log);
		}
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 */
	public void scrapeFromJson(String pageInstructionJSON)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, new Hashtable[] { new Hashtable() });
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String pageInstructionJSON, Hashtable defaults)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, new Hashtable[] { defaults } );
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String for each member of <code>defaultsArray</code>.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String pageInstructionJSON, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, defaultsArray);
	}

	/**
	 * Scrape from a {@link Page} loaded from a URI.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 */
	public void scrapeFromUri(String uri)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, new Hashtable[] { new Hashtable() });
	}
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 */
	public void scrapeFromUri(String uri, Hashtable defaults)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, new Hashtable[] { defaults } );
	}

	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromUri(String uri, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, DatabaseException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, defaultsArray);
	}

	/**
	 * Set the rate limit for loading from a single host. Microscraper will wait until the rate is below this
	 * threshold before making another request.
	 * Set this to 0 to disable rate limiting.
	 * @param rateLimitKBPS The rate limit to use.
	 * @see Browser#setRateLimit(int)
	 */
	public void setRateLimit(int rateLimitKBPS) {
		browser.setRateLimit(rateLimitKBPS);
	}

	/**
	 * @param timeout How many seconds before giving up on a request.
	 * @see Browser#setTimeout(int)
	 */
	public void setTimeout(int timeout) {
		browser.setTimeout(timeout);
	}

	/**
	 * @param maxResponseSizeKB The maximum size of a single response in kilobytes that Microscraper will
	 * load before terminating. Since responses are fed straight through to a regex parser,
	 * it is wise not to deal with huge pages.
	 * @see Browser#setMaxResponseSize(int)
	 */
	public void setMaxResponseSize(int maxResponseSizeKB) {
		browser.setMaxResponseSize(maxResponseSizeKB);
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
