package net.microscraper.resources;

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
	 * 
	 * @return a copy of the variables for this context.
	 */
	public Variables getVariables() {
		return null;
	}
}
