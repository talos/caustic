package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

public final class ParserOneToMany extends Parser {

	protected ParserOneToMany(Reference ref, Regexp searchRegexp,
			MustacheTemplate replacement) {
		super(ref, searchRegexp, replacement);
	}
	
	public String[] parse(String input, ExecutionContext context) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		try {
			Pattern pattern = generatePattern(context);
			String replacement = generateReplacement(context);
			return pattern.allMatches(input, replacement);
		} catch (NoMatches e) {
			throw new ExecutionFailure(e, this);
		} catch (MissingGroup e) {
			throw new ExecutionFatality(e, this);
		}
	}
}
