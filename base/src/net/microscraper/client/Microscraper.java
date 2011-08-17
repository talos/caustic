package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Load;
import net.microscraper.json.JsonParser;
import net.microscraper.json.JsonObject;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.UriFactory;
import net.microscraper.uri.Uri;
import net.microscraper.util.BasicVariables;
import net.microscraper.util.StringUtils;

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
	private final Uri userDir;
	private final RegexpCompiler compiler;
	private final Browser browser;
	private final UriFactory uriFactory;
	private final Database database;
	private final JsonParser parser;
	private final BasicLog log = new BasicLog();
	
	/**
	 * @param compiler The {@link RegexpCompiler} to use when compiling regular
	 * expressions.
	 * @param browser A {@link Browser} to use for HTTP requests.
	 * @param jsonInterface A {@link JsonParser} to use in parsing and loading JSON.
	 * @param uriFactory A {@link UriFactory} to create {@link Uri}s with.
	 * @param database the {@link Database} to use for storage.
	 */
	public Microscraper(RegexpCompiler compiler, Browser browser, UriFactory uriFactory,
			JsonParser jsonInterface, Database database) {
		this.compiler = compiler;
		this.browser = browser;
		this.parser = jsonInterface;
		this.database = database;
		this.uriFactory = uriFactory;
		try {
			this.userDir = uriFactory.fromString(System.getProperty("user.dir"));
		} catch(MalformedUriException e) {
			throw new RuntimeException(StringUtils.quote(System.getProperty("user.dir")) + " could not be converted to URI.");
		}
		this.browser.register(log);
	}
	
	private void scrape(JsonObject pageJson, Hashtable[] defaultsHash)
			throws DeserializationException, IOException {
		for(int i = 0 ; i < defaultsHash.length ; i ++) {
			new Load(pageJson).execute(compiler, browser, BasicVariables.fromHashtable(defaultsHash[i]), null, database, log);
		}
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 */
	public void scrapeFromJson(String pageInstructionJSON)
			throws DeserializationException, IOException {
		JsonObject json = parser.parse(userDir, pageInstructionJSON);
		scrape(json, new Hashtable[] { new Hashtable() });
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String pageInstructionJSON, Hashtable defaults)
			throws DeserializationException, IOException, InterruptedException {
		JsonObject json = parser.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, new Hashtable[] { defaults } );
	}
	
	/**
	 * Scrape from a {@link Load} in a JSON String for each member of <code>defaultsArray</code>.
	 * @param pageInstructionJSON A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromJson(String pageInstructionJSON, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, InterruptedException {
		JsonObject json = parser.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, defaultsArray);
	}

	/**
	 * Scrape from a {@link Load} loaded from a URI.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 */
	public void scrapeFromUri(String uri)
			throws DeserializationException, IOException, InterruptedException {
		JsonObject json = parser.load(this.uriFactory.fromString(uri));
		scrape(json, new Hashtable[] { new Hashtable() });
	}
	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 */
	public void scrapeFromUri(String uri, Hashtable defaults)
			throws DeserializationException, IOException, InterruptedException {
		JsonObject json = parser.load(this.uriFactory.fromString(uri));
		scrape(json, new Hashtable[] { defaults } );
	}

	/**
	 * Scrape from a {@link Load} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>pageInstructionJSON</code> {@link MustacheTemplate} tags.
	 */
	public void scrapeFromUri(String uri, Hashtable[] defaultsArray)
			throws DeserializationException, IOException, InterruptedException {
		JsonObject json = parser.load(this.uriFactory.fromString(uri));
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
