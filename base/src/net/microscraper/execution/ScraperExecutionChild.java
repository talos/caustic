package net.microscraper.execution;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.model.Link;

/**
 * {@link ScraperExecutionChild} returns variable mappings for all parent contexts.  It prefers
 * closer mappings.
 * @author realest
 *
 */
public final class ScraperExecutionChild extends ScraperExecution {
	private final String extraName;
	private final String extraValue;
	private final Variables parentVariables;
	
	public ScraperExecutionChild(ExecutionContext context, 
			Link pipe, Execution parent, Variables parentVariables) {
		super(context, pipe, new UnencodedNameValuePair[] { }, parent);
		//super(log,resourceLoader,browser, regexpInterface, pipe, , parent );
		this.extraName = null;
		this.extraValue = null;
		this.parentVariables = parentVariables;
	}
	public ScraperExecutionChild(ExecutionContext context, 
			Link pipe, Execution parent, Variables parentVariables,
			String extraName, String extraValue) {
		super(context, pipe,
				new UnencodedNameValuePair[] { 
					new UnencodedNameValuePair(extraName, extraValue)
				}, parent);
		//super(pipe, context, new UnencodedNameValuePair[] { new UnencodedNameValuePair(extraName, extraValue) }, parent);
		this.extraName = extraName;
		this.extraValue = extraValue;
		this.parentVariables = parentVariables;
	}
	
	public String get(String key) throws MissingVariableException {
		if(super.containsKey(key) == false) {
			return parentVariables.get(key);
		} else{
			return super.get(key);
		}
	}
	
	public boolean containsKey(String key) {
		if(super.containsKey(key) == false) {
			return parentVariables.containsKey(key);
		}
		return true;
	}

	public boolean hasPublishName() {
		if(extraName != null)
			return true;
		return false;
	}

	public String getPublishName() {
		if(hasPublishName())
			return extraName;
		return null;
	}

	public boolean hasPublishValue() {
		if(extraValue != null)
			return true;
		return false;
	}

	public String getPublishValue() {
		if(hasPublishValue())
			return extraValue;
		return null;
	}
}
