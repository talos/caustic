package net.microscraper.resources;

public interface Executable {
	public Result execute(ExecutionContext context)
		throws ExecutionDelay, ExecutionFailure, ExecutionFatality;
}
