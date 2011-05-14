package net.microscraper.resources.definitions;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;
import net.microscraper.resources.Result;

public class OneToOneLink implements Link {
	private final Parsable from;
	private final OneToOneParser parser;
	public OneToOneLink(Parsable from, OneToOneParser to) {
		this.from = from;
		this.parser = to;
	}
	
	public Result[] execute(ExecutionContext context) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		String input = from.parse(context);
		return new Result[] { new Result(parser.parse(input, context)) };
	}
}
