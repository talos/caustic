package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.model.DeserializationException;
import net.microscraper.model.Parsable;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;

/**
 * {@link ParsableExecution}s are the {@link BasicExecution} implementation of {@link Parsable}s, and are contained inside
 * {@link Scraper}s but link to a {@link Parser} resource.  If one-to-many,
 * they are subclassed as {@link LeafExecution}; if one-to-one, they are
 * subclassed as {@link VariableExecution}.
 * @see LeafExectuion
 * @see VariableExecution
 * @see BasicExecution
 * @see Execution
 * @author john
 * 
 */
public abstract class ParsableExecution extends BasicExecution {
	/**
	 * The {@link Parsable} resource that spawned this execution.
	 */
	private final Parsable parsable;
	
	/**
	 * Private parser instance, saved after {@link #generateResource}.
	 */
	private Parser parser;
	
	public ParsableExecution(ExecutionContext context, Parsable parsable,
			Execution caller) {
		super(context, parsable.getParserLink().location, caller);
		
		this.parsable = parsable;
	}
	
	/**
	 * @return Whether the {@link #parsable} has a name.
	 * @see Parsable
	 */
	public final boolean hasName() {
		return parsable.hasName();
	}

	/**
	 * @return The {@link #parsable}'s name.
	 * @see Parsable
	 */
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
