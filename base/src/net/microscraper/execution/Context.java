package net.microscraper.execution;

import java.io.IOException;
import java.net.URL;

import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.Page;
import net.microscraper.model.Parser;
import net.microscraper.model.Scraper;
import net.microscraper.client.Browser;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Interfaces.Regexp;

public final class Context implements ResourceLoader, net.microscraper.client.Interfaces.Regexp, 
	net.microscraper.client.Interfaces.JSON, Browser, net.microscraper.client.Interfaces.Logger {
	
	private final ResourceLoader resourceLoader;
	private final net.microscraper.client.Interfaces.Regexp regexp;
	private final net.microscraper.client.Interfaces.JSON json;
	private final Browser browser;
	private final net.microscraper.client.Interfaces.Logger logger;
	private final String encoding;
	
	/**
	 * @param resourceLoader The {@link ResourceLoader} this {@link ScraperExecution} is set to use.
	 * @param browser The {@link Browser} this {@link ScraperExecution} is set to use.
	 * @param logger The {@link Logger} this {@link ScraperExecution} is set to use.
	 * @param encoding The encoding to use when encoding post data and cookies. "UTF-8" is recommended.
	 * @param regexp The {@link Regexp} interface to use when compiling regexps.
	 */
	public Context(ResourceLoader resourceLoader, net.microscraper.client.Interfaces.Regexp regexp,
			net.microscraper.client.Interfaces.JSON json, Browser browser,
			net.microscraper.client.Interfaces.Logger logger, String encoding) {
		this.resourceLoader = resourceLoader;
		this.regexp = regexp;
		this.json = json;
		this.browser = browser;
		this.logger = logger;
		this.encoding = encoding;
	}
	
	public Parser loadParser(Link link) throws IOException,
			DeserializationException {
		return resourceLoader.loadParser(link);
	}

	public Scraper loadScraper(Link link) throws IOException,
			DeserializationException {
		return resourceLoader.loadScraper(link);
	}

	public Page loadPage(Link link) throws IOException,
			DeserializationException {
		return resourceLoader.loadPage(link);
	}

	public void head(URL url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies) throws DelayRequest,
			BrowserException {
		browser.head(url, headers, cookies);
	}

	public String get(URL url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies, Pattern[] terminates)
			throws DelayRequest, BrowserException {
		return browser.get(url, headers, cookies, terminates);
	}

	public String post(URL url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies, Pattern[] terminates,
			EncodedNameValuePair[] posts) throws DelayRequest, BrowserException {
		return browser.post(url, headers, cookies, terminates, posts);
	}

	public void e(Throwable e) {
		logger.e(e);
	}

	public void w(Throwable w) {
		logger.w(w);
	}

	public void i(String infoText) {
		logger.i(infoText);
	}

	public Tokener getTokener(String jsonString) throws JSONInterfaceException {
		return json.getTokener(jsonString);
	}

	public Stringer getStringer() throws JSONInterfaceException {
		return json.getStringer();
	}

	public Pattern compile(String attributeValue, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		return regexp.compile(attributeValue, isCaseInsensitive, isMultiline, doesDotMatchNewline);
	}
	
	public String getEncoding() {
		return encoding;
	}
}
