package net.microscraper;

import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * This class holds interfaces needed to run {@link Executable}s.
 * @author john
 *
 */
public final class Interfaces {
	private final Log log;
	public final Log getLog() {
		return log;
	}
	
	private final RegexpCompiler regexpCompiler;
	public final RegexpCompiler getRegexpCompiler() {
		return regexpCompiler;
	}
	
	private final Browser browser;
	public final Browser getBrowser() {
		return browser;
	}
	
	private final JSONInterface jsonInterface;
	public final JSONInterface getJSONInterface() {
		return jsonInterface;
	}
	
	//public final String encoding;
	public Interfaces(Log log, RegexpCompiler regexpInterface,
			Browser browser, JSONInterface jsonInterface) {
		this.log = log;
		this.regexpCompiler = regexpInterface;
		this.browser = browser;
		this.jsonInterface = jsonInterface;
		//this.encoding = encoding;
	}
}
