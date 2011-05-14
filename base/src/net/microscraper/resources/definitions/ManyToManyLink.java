package net.microscraper.resources.definitions;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;
import net.microscraper.resources.Result;

public class ManyToManyLink implements Link {
	private final OneToManyParser from;
	private final OneToOneParser parser;
	public ManyToManyLink(OneToManyParser from, OneToOneParser to) {
		this.from = from;
		this.parser = to;
	}
	public Result[] execute(ExecutionContext context) throws ExecutionDelay,
			ExecutionFailure, ExecutionFatality {
		// TODO Auto-generated method stub
		return null;
	}

}
