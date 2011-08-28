package net.microscraper.client;

import java.io.File;
import java.io.IOException;

import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.file.JavaIOFileLoader;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.json.JsonDeserializer;
import net.microscraper.json.JsonMEParser;
import net.microscraper.json.JsonParser;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.JavaNetURILoader;
import net.microscraper.uri.JavaNetUriResolver;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;

/**
 * Basic implementation of {@link Microscraper} using
 * {@link JavaUtilRegexpCompiler}, {@link JavaNetHttpRequester},
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
			HttpRequester requester = new JavaNetHttpRequester();
			requester.setTimeout(timeout);
			
			RateLimitManager memory = new RateLimitManager(new JavaNetHttpUtils(), rateLimit);
			
			HttpBrowser browser = new HttpBrowser(new JavaNetHttpRequester(),
					memory, new JavaNetCookieManager());
			
			String executionDir = new File(System.getProperty("user.dir")).toURI().toString();
			if(!executionDir.endsWith("/")) {
				executionDir += "/";
			}
			
			RegexpCompiler compiler = new JavaUtilRegexpCompiler();
			URILoader uriLoader = new JavaNetURILoader(browser, new JavaIOFileLoader());
			UriResolver uriResolver = new JavaNetUriResolver();
			JsonParser parser = new JsonMEParser();
			Encoder encoder = new JavaNetEncoder();
			Deserializer deserializer = new JsonDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
			Microscraper scraper = new Microscraper(deserializer, database, executionDir);
			return scraper;
		} catch(MalformedUriException e) {
			throw new IOException(e);
		}

	}
}
