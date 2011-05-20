package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.model.DeserializationException;
import net.microscraper.model.Parsable;
import net.microscraper.model.Parser;

public abstract class ParsableExecution extends BasicExecution {
	private final Parsable parsable;
	private final Context context;
	
	private Parser parser;
	
	public ParsableExecution(Context context, Parsable parsable, Execution caller) {
		super(context, parsable.getParserLink().location, caller);
		
		this.parsable = parsable;
		this.context = context;		
	}
	
	public boolean hasName() {
		return parsable.hasName();
	}
	
	public String getName() {
		return parsable.getName();
	}
	
	public Parser getParser() throws IOException, DeserializationException {
		if(parser != null) {
			return parser;
		} else {
			return context.loadParser(parsable.getParserLink());
		}
	}
	
	public boolean hasPublishName() {
		return hasName();
	}

	public String getPublishName() {
		return getName();
	}
}
