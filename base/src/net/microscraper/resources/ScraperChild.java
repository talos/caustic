package net.microscraper.resources;

import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Log;
import net.microscraper.client.MissingReference;
import net.microscraper.resources.definitions.LinkToOne;
import net.microscraper.resources.definitions.Reference;

public class ScraperChild extends Scraper  {
	private final Scraper parentContext;
	private final Hashtable variables = new Hashtable();
	protected ScraperChild(Browser browser, Log log, String encoding,
			Regexp regexp, Scraper parentContext, LinkToOne[] links) {
		super(browser, log, encoding, regexp, links);
		this.parentContext = parentContext;
	}

	public void put(Reference ref, String result) {
		variables.put(ref, result);
	}

	public String get(Reference ref) throws MissingReference {
		Object result = variables.get(ref);
		if(result == null) {
			result = parentContext.get(ref);
		}
		return (String) result;
	}
}
