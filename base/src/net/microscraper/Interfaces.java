package net.microscraper;

import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Connection;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.log.Logger;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * This class holds interfaces shared by {@link Executable}s.
 * @author john
 *
 */
public final class Interfaces {
	private final Logger logger;
	
	/**
	 * 
	 * @return A shared {@link Logger}.
	 */
	public final Logger getLog() {
		return logger;
	}
	
	private final RegexpCompiler regexpCompiler;
	
	/**
	 * 
	 * @return A shared {@link RegexpCompiler}.
	 */
	public final RegexpCompiler getRegexpCompiler() {
		return regexpCompiler;
	}
	
	private final Browser browser;
	/**
	 * 
	 * @return A shared {@link Browser}.
	 */
	public final Browser getBrowser() {
		return browser;
	}
	
	private final JSONInterface jsonInterface;
	/**
	 * 
	 * @return A shared {@link JSONInterface}
	 */
	public final JSONInterface getJSONInterface() {
		return jsonInterface;
	}
	
	private final Database database;
	/**
	 * 
	 * @return A shared {@link Database}.
	 */
	public final Database getDatabase() {
		return database;
	}
	
	public Interfaces(Logger logger, RegexpCompiler regexpInterface,
			Browser browser, JSONInterface jsonInterface, Database database) {
		this.logger = logger;
		this.regexpCompiler = regexpInterface;
		this.browser = browser;
		this.jsonInterface = jsonInterface;
		this.database = database;
	}
}
