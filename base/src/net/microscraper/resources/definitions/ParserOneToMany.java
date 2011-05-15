package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFailure;
import net.microscraper.resources.ScrapingFatality;

public final class ParserOneToMany extends Parser {

	protected ParserOneToMany(Reference ref, Regexp search,
			MustacheTemplate replacement) {
		super(ref, search, replacement);
	}
	
	public String[] parse(String input, Scraper context) throws ScrapingDelay, ScrapingFailure, ScrapingFatality {
		try {
			Pattern pattern = generatePattern(context);
			String replacement = generateReplacement(context);
			return pattern.allMatches(input, replacement);
		} catch (NoMatches e) {
			throw new ScrapingFailure(e, this);
		} catch (MissingGroup e) {
			throw new ScrapingFatality(e, this);
		}
	}
}
