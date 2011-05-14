package net.microscraper.resources;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.ParserOneToMany;
import net.microscraper.resources.definitions.Reference;

/**
 * The context within which an executable is executed.  This contains a set of variables
 * that can be used for substitutions.  ExecutionContexts amass as many variables (from
 * parsers) as possible, but branch at OneToMany parsers.
 * @author realest
 *
 */
public abstract class ExecutionContext {
	private final Browser browser;
	private final Log log;
	private final String encoding;
	private final Regexp regexp;
	
	/**
	 * The Browser this ExecutionContext is set to use.
	 */
	public Browser getBrowser() {
		return browser;
	}
	
	/**
	 * The Log this ExecutionContext is set to use.
	 */
	public Log getLog() {
		return log;
	}
	
	/**
	 * The encoding to use when encoding post data and cookies.
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * The Regexp interface to use when compiling regexps.
	 */
	public Regexp getRegexp() {
		return regexp;
	}

	/**
	 * Put value of ref within this ExecutionContext.
	 * @param {@link Reference} ref to get value for.
	 * @param String value for reference.
	 */
	public abstract void put(Reference ref, String result);
	
	/**
	 * Get value of ref within this ExecutionContext.
	 * @param {@link Reference} ref to get value for.
	 * @return String value of ref.
	 */
	public abstract String get(Reference ref) throws MissingReference;
	
	/**
	 * Branch this execution into others based off of a LinkToMany.
	 * @param branchingParser
	 * @param result
	 * @return
	 */
	public ExecutionChild[] branch(ParserOneToMany branchingParser, String[] results) {
		ExecutionChild[] children = new ExecutionChild[results.length];
		for(int i = 0 ; i < children.length ; i ++) {
			children[i] = new ExecutionChild(getBrowser(), getLog(), getEncoding(),
					getRegexp(), this);
			children[i].put(branchingParser.getRef(), results[i]);
		}
		return children;
	}
	
	protected ExecutionContext(Browser browser, Log log, String encoding, Regexp regexp) {
		this.browser = browser;
		this.log = log;
		this.encoding = encoding;
		this.regexp = regexp;
	}
}
