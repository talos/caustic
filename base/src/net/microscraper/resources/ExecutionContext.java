package net.microscraper.resources;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.Variables;

/**
 * The context within which an executable is executed.  This contains a set of variables
 * that can be used for substitutions.  ExecutionContexts amass as many variables (from
 * parsers) as possible, but branch at OneToMany parsers.
 * @author realest
 *
 */
public class ExecutionContext {

	/**
	 * The Browser this ExecutionContext is set to use.
	 */
	public final Browser browser;
	
	
	/**
	 * The Log this ExecutionContext is set to use.
	 */
	public final Log log;
	
	/**
	 * The encoding to use when encoding post data and cookies.
	 */
	public final String encoding;
	
	/**
	 * The Regexp interface to use when compiling regexps.
	 */
	public final Regexp regexp;
	
	private final Variables parentVariables;
	private final Variables variables = new Variables();
	
	public ExecutionContext(Browser browser, Log log, String encoding, Regexp regexp, Variables parentVariables) {
		this.browser = browser;
		this.log = log;
		this.encoding = encoding;
		this.regexp = regexp;
		this.parentVariables = parentVariables;
	}
	
	/**
	 * 
	 * @return a copy of the variables for this context.
	 */
	public Variables getVariables() {
		return variables.extend(parentVariables);
	}
	
	public void addVariable(String name, String value) {
		variables.put(name, value);
	}
	
	/**
	 * @param executable the Executable that caused the branch.
	 * @return a branch of the ExecutionContext, which will inherit its parent's variables but not affect them.
	 */
	public ExecutionContext branch(Executable executable) {
		return new ExecutionContext(browser, log, encoding, regexp, variables);
	}
}
