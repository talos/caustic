package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFailure;
import net.microscraper.resources.ScrapingFatality;

public abstract class Parser implements Problematic, Variable {
	private final Reference ref;
	private final Regexp search;
	private final MustacheTemplate replacement;
	protected Parser(Reference ref, Regexp search, MustacheTemplate replacement) {
		this.ref = ref;
		this.search = search;
		this.replacement = replacement;
	}
	public final Reference getRef() {
		return ref;
	}
	protected final Pattern generatePattern(Scraper context)
				throws ScrapingDelay, ScrapingFailure, ScrapingFatality {
		return search.getPattern(context);
	}
	
	protected final String generateReplacement(Scraper context)
				throws ScrapingDelay, ScrapingFailure, ScrapingFatality {
		return replacement.getString(context);
	}

	public String getName() {
		return ref.toString();
	}
}
