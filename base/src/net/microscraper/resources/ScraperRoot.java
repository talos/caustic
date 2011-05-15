package net.microscraper.resources;

import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.LinkToOne;
import net.microscraper.resources.definitions.Reference;

public class ScraperRoot extends Scraper {
	private Hashtable variables = new Hashtable();
	
	/**
	 * 
	 * @param browser {@link Browser} The browser to use when executing with this context.
	 * @param log {@link Log} The log to use when executing within this context.
	 * @param encoding The encoding to use within this context.
	 * @param regexp {@link Regexp} The regexp interface to use within this context.
	 * @param links {@link LinkToOne} The links active within this scraper.
	 */
	public ScraperRoot(Browser browser, Log log, String encoding,
			Regexp regexp, LinkToOne[] links) {
		super(browser, log, encoding, regexp, links);
	}
	
	public void put(Reference ref, String result) {
		variables.put(ref, result);
	}
	
	public String get(Reference ref) throws MissingReference {
		Object result = variables.get(ref);
		if(result == null) {
			throw new MissingReference(ref);
		} else {
			return (String) result;
		}
	}
}
