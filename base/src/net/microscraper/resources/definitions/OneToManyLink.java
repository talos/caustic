package net.microscraper.resources.definitions;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;
import net.microscraper.resources.Result;

public class OneToManyLink implements Link {
	private final Parsable from;
	private final OneToManyParser parser;
	public OneToManyLink(Parsable from, OneToManyParser to) {
		this.from = from;
		this.parser = to;
	}
	public Result[] execute(ExecutionContext context) throws ExecutionDelay,
			ExecutionFailure, ExecutionFatality {
		String input = from.parse(context);
		String[] output = parser.parse(input, context);
		Result[] results = new Result[output.length];
		for(int i = 0 ; i < output.length ; i ++ ) {
			results[i] = new Result(output[i]);
		}
		return results;
	}
}
