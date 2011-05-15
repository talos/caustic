package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

public final class Regexp {
	private final MustacheTemplate pattern;
	private final boolean isCaseInsensitive;
	private final boolean isMultiline;
	private final boolean doesDotMatchNewline;
	public Regexp (MustacheTemplate pattern,
					boolean isCaseInsensitive, boolean isMultiline,
					boolean doesDotMatchNewline) {
		this.pattern = pattern;
		this.isCaseInsensitive = isCaseInsensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
	public Pattern getPattern(Scraper context) throws ScrapingDelay, ScrapingFatality {
		String pattern = this.pattern.getString(context);
		return context.getRegexp().compile(pattern, isCaseInsensitive, isMultiline, doesDotMatchNewline);
	}
}