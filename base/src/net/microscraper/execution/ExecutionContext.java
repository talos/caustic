package net.microscraper.execution;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Log;

/**
 * This class holds unchanging interfaces needed to run executions.
 * @author john
 *
 */
public final class ExecutionContext {
	public final Log log;
	public final Interfaces.Regexp regexpInterface;
	public final Browser browser;
	public final ResourceLoader resourceLoader;
	public final String encoding;
	public ExecutionContext(Log log, Interfaces.Regexp regexpInterface,
			Browser browser, ResourceLoader resourceLoader, String encoding) {
		this.log = log;
		this.regexpInterface = regexpInterface;
		this.browser = browser;
		this.resourceLoader = resourceLoader;
		this.encoding = encoding;
	}
}
