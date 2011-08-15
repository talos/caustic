package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.Database;
import net.microscraper.impl.log.Log;
import net.microscraper.instruction.Executable;
import net.microscraper.instruction.Page;
import net.microscraper.json.JSONParser;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.URIFactory;
import net.microscraper.uri.URIInterface;
import net.microscraper.util.BasicVariables;
import net.microscraper.util.Utils;

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
public class Microscraper extends Log {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle this warning differently
	
	private final String userDir;
	private final RegexpCompiler compiler;
	private final Browser browser;
	private final URIFactory uriFactory;
	private final Database database;
	private final JSONParser jsonInterface;
	
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
	}
	
	private void scrape(JSONObjectInterface pageJson, String urlEncodedDefaults, Hashtable extraDefaults) throws MicroscraperException {
		try {
			Page page = new Page(pageJson);
			BasicVariables variables;
			if(urlEncodedDefaults != null) {
				variables = BasicVariables.fromFormEncoded(browser, urlEncodedDefaults, Browser.UTF_8);
			} else {
				variables = BasicVariables.empty();
			}
			if(extraDefaults != null) {
				variables.extend(extraDefaults);
			}
			
			// Create & initially stock queue.
			Vector queue = new Vector();
			queue.add(new Executable(page, compiler,
					browser, variables, null, database));
			
			// Run queue.
			while(queue.size() > 0) {
				if(queue.size() > LARGE_QUEUE) {
					i("Large execution queue: " + Utils.quote(queue.size()));
				}
				Executable exc = (Executable) queue.elementAt(0);
				queue.removeElementAt(0);
				i("Running " + exc.toString());
				exc.run();
				// If the execution is complete, add its children to the queue.
				if(exc.isComplete()) {
					Executable[] children = exc.getChildren();
					Utils.arrayIntoVector(children, queue);
				} else if (exc.isStuck()) {
					i(Utils.quote(exc.toString()) + " is stuck on " + Utils.quote(exc.stuckOn()));
				} else if (exc.hasFailed()) {
					w(exc.failedBecause());
				// If the execution is not stuck and is not failed, return it to the end queue.
				} else {
					queue.addElement(exc);
				}
			}
		} catch(IOException e) {
			throw new MicroscraperException(e);
		}
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithJSON(String pageInstructionJSON) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, null, null);
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 * @param urlEncodedDefaults A URL encoded {@link String} of default values to use when scraping.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithJSON(String pageInstructionJSON, String urlEncodedDefaults) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, urlEncodedDefaults, null);
	}
	
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param pageInstructionJSON A {@link String} with a {@link Page} serialized in JSON.
	 * @param urlEncodedDefaults A URL encoded {@link String} of default values to use when scraping.
	 * @param extraDefaults A {@link Hashtable} of values to add to <code>urlEncodedDefaults</code>.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithJSON(String pageInstructionJSON, String urlEncodedDefaults, Hashtable extraDefaults) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.parse(uriFactory.fromString(userDir), pageInstructionJSON);
		scrape(json, urlEncodedDefaults, extraDefaults);
	}

	/**
	 * Scrape from a {@link Page} loaded from a URI.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithURI(String uri) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, null, null);
	}
	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 * @param urlEncodedDefaults A URL encoded {@link String} of default values to use when scraping.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithURI(String uri, String urlEncodedDefaults) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, urlEncodedDefaults, null);
	}

	/**
	 * Scrape from a {@link Page} in a JSON String.
	 * @param uri A {@link String} with the URI location of a {@link Page} serialized in JSON.
	 * @param urlEncodedDefaults A URL encoded {@link String} of default values to use when scraping.
	 * @param extraDefaults A {@link Hashtable} of values to add to <code>urlEncodedDefaults</code>.
	 * @throws MicroscraperException If there was an error scraping.
	 */
	public void scrapeWithURI(String uri, String urlEncodedDefaults, Hashtable extraDefaults) throws MicroscraperException {
		JSONObjectInterface json = jsonInterface.load(this.uriFactory.fromString(uri));
		scrape(json, urlEncodedDefaults, extraDefaults);
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
}
