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
	private final Variables parent;
	private final String extraName;
	private final String extraValue;
	
	public ScraperExecutionChild(Link pipe, Context context, Variables parent) {
		super(pipe, context, new UnencodedNameValuePair[] { }, parent );
		this.parent = parent;
		this.extraName = null;
		this.extraValue = null;
	}
	public ScraperExecutionChild(Link pipe, Context context, Variables parent,
			String extraName, String extraValue) {
		super(pipe, context, new UnencodedNameValuePair[] { new UnencodedNameValuePair(extraName, extraValue) }, parent);
		this.parent = parent;
		this.extraName = extraName;
		this.extraValue = extraValue;
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
