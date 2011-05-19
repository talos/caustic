package net.microscraper.execution;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.model.Link;

/**
 * {@link ScraperExecutionChild} returns variable mappings for all parent contexts.  It prefers
 * closer mappings.
 * @author realest
 *
 */
public final class ScraperExecutionChild extends ScraperExecution {
	private final HasVariableExecutions parent;
	public ScraperExecutionChild(Link pipe, Context context, HasVariableExecutions parent) {
		super(pipe, context, new UnencodedNameValuePair[] { } );
		this.parent = parent;
	}
	public ScraperExecutionChild(Link pipe, Context context, HasVariableExecutions parent,
			String extraName, String extraValue) {
		super(pipe, context, new UnencodedNameValuePair[] { new UnencodedNameValuePair(extraName, extraValue) });
		this.parent = parent;
	}
	
	public String get(String key) throws MissingVariableException {
		if(super.containsKey(key) == false) {
			return parent.get(key);
		} else{
			return super.get(key);
		}
	}
	
	public boolean containsKey(String key) {
		if(super.containsKey(key) == false) {
			return parent.containsKey(key);
		}
		return true;
	}
}
