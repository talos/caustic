package net.microscraper.resources.definitions;

import net.microscraper.client.MissingReference;
import net.microscraper.resources.Scraper;

public abstract class Link {
	private final Variable from;
	protected Link(Variable from) {
		this.from = from;
	}
	public final String getFromString(Scraper context) throws MissingReference {
		return context.get(from.getRef());
	}
}
