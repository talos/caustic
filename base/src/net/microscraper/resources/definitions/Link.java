package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;
import net.microscraper.resources.Result;

public interface Link {
	public abstract Result[] execute(ExecutionContext context) throws ExecutionDelay, ExecutionFailure, ExecutionFatality;
}
