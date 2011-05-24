package net.microscraper.client;

import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.RegexpCompiler;

/**
 * This class holds unchanging interfaces needed to run executions.
 * @author john
 *
 */
public final class ExecutionContext {
	public final Log log;
	public final RegexpCompiler regexpInterface;
	public final Browser browser;
	public final ResourceLoader resourceLoader;
	public final String encoding;
	public ExecutionContext(Log log, RegexpCompiler regexpInterface,
			Browser browser, ResourceLoader resourceLoader, String encoding) {
		this.log = log;
		this.regexpInterface = regexpInterface;
		this.browser = browser;
		this.resourceLoader = resourceLoader;
		this.encoding = encoding;
	}
}
