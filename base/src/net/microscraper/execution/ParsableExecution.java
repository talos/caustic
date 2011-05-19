package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.client.Variables;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Parsable;
import net.microscraper.model.Parser;

public abstract class ParsableExecution implements Execution {
	public final Parsable parsable;
	private Parser parser;
	private Exception failure = null;
	//public VariableExecution(MustacheCompiler mustache, ResourceLoader loader, Variable variable, String stringToParse) {
	public ParsableExecution(ResourceLoader loader, Parsable parsable) {
		this.parsable = parsable;
		
		try {
			this.parser = loader.loadParser(parsable.getParserLink());
		} catch(IOException e) {
			failure = e;
		} catch(DeserializationException e) {
			failure = e;
		}
	}
	public boolean hasName() {
		return parsable.hasName();
	}
	public String getName() {
		return parsable.getName();
	}
	public Parser getParser() {
		return parser;
	}
	
	public boolean hasFailed() {
		if(failure != null)
			return true;
		return false;
	}
}
