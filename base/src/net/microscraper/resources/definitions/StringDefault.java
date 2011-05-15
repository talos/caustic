package net.microscraper.resources.definitions;

import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

public class StringDefault implements Variable, Stringable, Problematic {
	private final Reference ref;
	private final MustacheTemplate template;

	/**
	 * @param ref {@link Reference} A ref to uniquely identify the StringDefault.
	 * @param template {@link MustacheTemplate} The template compiled to obtain the String default.
	 */
	public StringDefault(Reference ref, MustacheTemplate template) {
		this.ref = ref;
		this.template = template;
	}
	public Reference getRef() {
		return ref;
	}
	public String getString(Scraper context) throws ScrapingDelay, ScrapingFatality {
		return template.getString(context);
	}
	public String getName() {
		return ref.toString();
	}
}
