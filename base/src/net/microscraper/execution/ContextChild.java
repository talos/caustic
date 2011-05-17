package net.microscraper.execution;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.model.Scraper;

/**
 * ContextChild returns variable mappings for all parent contexts.  It prefers
 * closer mappings.
 * @author realest
 *
 */
public final class ContextChild extends ContextRoot {
	private final ContextRoot parent;
	public ContextChild(Scraper scraper, ResourceLoader resourceLoader, Browser browser,
			Log log, String encoding, Regexp regexp, ContextRoot parent) {
		super(scraper, resourceLoader, browser, log, encoding, regexp,
				new UnencodedNameValuePair[] { } );
		this.parent = parent;
	}
	public ContextChild(Scraper scraper, ResourceLoader resourceLoader, Browser browser,
			Log log, String encoding, Regexp regexp, ContextRoot parent,
			String extraName, String extraValue) {
		super(scraper, resourceLoader, browser, log, encoding, regexp,
				new UnencodedNameValuePair[] { new UnencodedNameValuePair(extraName, extraValue) });
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
