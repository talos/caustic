package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.model.DeserializationException;
import net.microscraper.model.Parsable;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;

public abstract class ParsableExecution implements Execution {
	public final Parsable parsable;
	private Parser parser;
	private Exception failure = null;
	private final int id;
	private static int count = 0;
	//public VariableExecution(MustacheCompiler mustache, ResourceLoader loader, Variable variable, String stringToParse) {
	public ParsableExecution(ResourceLoader loader, Parsable parsable) {
		this.id = count;
		count++;
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
	
	public int getId() {
		return id;
	}

	public boolean hasPublishName() {
		return hasName();
	}

	public String getPublishName() {
		return getName();
	}
	
	public final Resource getResource() {
		return getParser();
	}
}
