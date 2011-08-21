package net.microscraper.client;

import java.io.IOException;

import net.microscraper.browser.JavaNetBrowser;
import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.file.FileLoader;
import net.microscraper.file.JavaIOFileLoader;
import net.microscraper.json.JsonDeserializer;
import net.microscraper.json.JsonMEParser;
import net.microscraper.json.JsonParser;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.JavaNetURIFactory;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.UriFactory;

/**
 * Basic implementation of {@link Microscraper} using
 * {@link JavaUtilRegexpCompiler}, {@link JavaNetBrowser},
 * {@link JavaNetURIFactory}, {@link JavaIOFileLoader},
 * and {@link JsonMEParser}.
 * @author realest
 *
 */
public class BasicMicroscraper {
	/**
	 * 
	 * @param database
	 * @return
	 * @throws IOException If {@link System#getProperty(String) for <code>user.dir</code>
	 * cannot be turned into the root {@link Uri} for local json references.
	 */
	public static Microscraper get(Database database, int rateLimit, int timeout) throws IOException {
		try {
			Browser browser = new JavaNetBrowser();
			browser.setRateLimit(rateLimit);
			browser.setTimeout(timeout);
			FileLoader fileLoader = new JavaIOFileLoader();
			RegexpCompiler compiler = new JavaUtilRegexpCompiler();
			UriFactory uriFactory = new JavaNetURIFactory(browser, fileLoader);
			JsonParser parser = new JsonMEParser(uriFactory.fromString(System.getProperty("user.dir")));
			Deserializer deserializer = new JsonDeserializer(parser, compiler, browser);
			return new Microscraper(deserializer, database, uriFactory);
		} catch(MalformedUriException e) {
			throw new IOException(e);
		}

	}
}
