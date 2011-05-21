package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Log;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Parsable;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;

public abstract class ParsableExecution extends BasicExecution {
	private final Parsable parsable;
	
	private Parser parser;
	
	public ParsableExecution(ExecutionContext context, Parsable parsable,
			Execution caller) {
		super(context, parsable.getParserLink().location, caller);
		
		this.parsable = parsable;
	}
	
	public final boolean hasName() {
		return parsable.hasName();
	}
	
	public final String getName() {
		return parsable.getName();
	}
	
	public final Resource generateResource(ExecutionContext context)
				throws IOException, DeserializationException {
		if(parser != null) {
			return parser;
		} else {
			return context.resourceLoader.loadParser(parsable.getParserLink());
		}
	}
	
	public final boolean hasPublishName() {
		return hasName();
	}

	public final String getPublishName() {
		return getName();
	}
}
