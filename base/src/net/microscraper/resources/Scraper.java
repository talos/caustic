package net.microscraper.resources;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.LinkToOne;
import net.microscraper.resources.definitions.ParserOneToMany;
import net.microscraper.resources.definitions.Reference;

/**
 * The context within which an executable is executed.  This contains a set of variables
 * that can be used for substitutions.  ExecutionContexts amass as many variables (from
 * parsers) as possible, but branch at OneToMany parsers.
 * @author realest
 *
 */
public abstract class Scraper {
	private final Browser browser;
	private final Log log;
	private final String encoding;
	private final Regexp regexp;
	
	/**
	 * @return {@link Browser} The Browser this Scraper is set to use.
	 */
	public Browser getBrowser() {
		return browser;
	}
	
	/**
	 * @return {@link Log} The Log this Scraper is set to use.
	 */
	public Log getLog() {
		return log;
	}
	
	/**
	 * @return The encoding to use when encoding post data and cookies.
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * @return {@link Regexp} The Regexp interface to use when compiling regexps.
	 */
	public Regexp getRegexp() {
		return regexp;
	}
	
	/**
	 * Put value of ref within this Scraper.
	 * @param {@link Reference} ref to get value for.
	 * @param String value for reference.
	 */
	public abstract void put(Reference ref, String result);
	
	/**
	 * Get value of ref within this Scraper.
	 * @param {@link Reference} ref to get value for.
	 * @return String value of ref.
	 */
	public abstract String get(Reference ref) throws MissingReference;
	
	/**
	 * Branch this execution into others based off of a LinkToMany.
	 * @param branchingParser
	 * @param result
	 */
	public void scrapeBranch(ParserOneToMany branchingParser,
			String[] results) {
		ScraperChild[] children = new ScraperChild[results.length];
		for(int i = 0 ; i < children.length ; i ++) {
			children[i] = new ScraperChild(getBrowser(), getLog(), getEncoding(),
					getRegexp(), this);
			children[i].put(branchingParser.getRef(), results[i]);
		}
	}
	
	protected Scraper(Browser browser, Log log, String encoding, Regexp regexp) {
		this.browser = browser;
		this.log = log;
		this.encoding = encoding;
		this.regexp = regexp;
	}
	
	public Results scrape(LinkToOne[] links) throws ScrapingFatality {
		Results results = new Results();
		for(int i = 0 ; i < links.length ; i ++) {
			
		}
		return results;
	}
}
