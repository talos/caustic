package net.microscraper.client.interfaces;

import net.microscraper.client.Log;

/**
 * This class holds interfaces needed to run {@link Executable}s.
 * @author john
 *
 */
public final class Interfaces {
	public final Log log;
	public final RegexpCompiler regexpCompiler;
	public final Browser browser;
	public final NetInterface netInterface;
	public final JSONInterface jsonInterface;
	public final String encoding;
	public Interfaces(Log log, RegexpCompiler regexpInterface,
			Browser browser, NetInterface netInterface,
			JSONInterface jsonInterface, String encoding) {
		this.log = log;
		this.regexpCompiler = regexpInterface;
		this.browser = browser;
		this.netInterface = netInterface;
		this.jsonInterface = jsonInterface;
		this.encoding = encoding;
	}
}
