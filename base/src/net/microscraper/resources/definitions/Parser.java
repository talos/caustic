package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Executable;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

public abstract class Parser implements Executable {
	private final Regexp searchRegexp;
	private final MustacheableString replacement;
	protected Parser(Regexp searchRegexp, MustacheableString replacement) {
		this.searchRegexp = searchRegexp;
		this.replacement = replacement;
	}
	
	//public abstract String[] parse(String source, ExecutionContext context);

	protected final Pattern generatePattern(ExecutionContext context)
				throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		return searchRegexp.getPattern(context);
	}
	
	protected final String generateReplacement(ExecutionContext context)
				throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		return replacement.parse(context);
	}
}
