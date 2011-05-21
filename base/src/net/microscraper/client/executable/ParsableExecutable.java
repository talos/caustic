package net.microscraper.client.executable;

import java.io.IOException;

import net.microscraper.client.ExecutionContext;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Parsable;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.Resource;

/**
 * {@link ParsableExecutable}s are the {@link BasicExecutable} implementation of {@link Parsable}s, and are contained inside
 * {@link Scraper}s but link to a {@link Parser} resource.  If one-to-many,
 * they are subclassed as {@link LeafExecutable}; if one-to-one, they are
 * subclassed as {@link VariableExecutable}.
 * @see LeafExectuion
 * @see VariableExecutable
 * @see BasicExecutable
 * @see Executable
 * @author john
 * 
 */
public abstract class ParsableExecutable extends BasicExecutable {
	/**
	 * The {@link Parsable} resource that spawned this execution.
	 */
	private final Parsable parsable;
	
	/**
	 * Private parser instance, saved after {@link #generateResource}.
	 */
	private Parser parser;
	
	public ParsableExecutable(ExecutionContext context, Parsable parsable,
			Executable caller) {
		super(context, parsable.getParserLink().location, caller);
		
		this.parsable = parsable;
	}
	
	/**
	 * @return Whether the {@link #parsable} has a name, which would be used as a
	 * key in {@link Variables}.
	 * @see Parsable
	 * @see Variables
	 */
	public final boolean hasName() {
		return parsable.hasName();
	}

	/**
	 * @return The {@link #parsable}'s name, used in {@link Variables}.
	 * @see Parsable
	 * @see Variables
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
}
