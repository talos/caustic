package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFailure;
import net.microscraper.resources.ScrapingFatality;

public final class ParserOneToOne extends Parser {
	private final int matchNumber;
	
	public ParserOneToOne(Reference ref, Regexp search,
			MustacheTemplate replacement, int matchNumber) {
		super(ref, search, replacement);
		this.matchNumber = matchNumber;
	}
	
	public String parse(String input, Scraper context) throws ScrapingDelay, ScrapingFailure, ScrapingFatality {
		try {
			Pattern pattern = generatePattern(context);
			String replacement = generateReplacement(context);
			return pattern.match(input, replacement, matchNumber);
		} catch (NoMatches e) {
			throw new ScrapingFailure(e, this);
		} catch (MissingGroup e) {
			throw new ScrapingFatality(e, this);
		}
	}
}
