package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

public final class ParserOneToOne extends Parser {
	private final int matchNumber;
	
	public ParserOneToOne(Reference ref, Regexp searchRegexp,
			MustacheTemplate replacement, int matchNumber) {
		super(ref, searchRegexp, replacement);
		this.matchNumber = matchNumber;
	}
	
	public String parse(String input, ExecutionContext context) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		try {
			Pattern pattern = generatePattern(context);
			String replacement = generateReplacement(context);
			return pattern.match(input, replacement, matchNumber);
		} catch (NoMatches e) {
			throw new ExecutionFailure(e, this);
		} catch (MissingGroup e) {
			throw new ExecutionFatality(e, this);
		}
	}
}
