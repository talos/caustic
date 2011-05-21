package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.InvalidRangeException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.Leaf;
import net.microscraper.server.resource.Link;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.Resource;

public class LeafExecutable extends ParsableExecutable {
	private String[] results = null;
	private final int minMatch;
	private final int maxMatch;
	private final String stringToParse;
	private final Link[] pipes;
	private final Variables variables;
	
	public LeafExecutable(ExecutionContext context,
			Executable parent, Variables variables,
			Leaf leaf, String stringToParse) {
		super(context, leaf, parent);
		this.stringToParse = stringToParse;
		this.maxMatch = leaf.maxMatch;
		this.minMatch = leaf.minMatch;
		this.variables = variables;
		this.pipes = leaf.getPipes();
	}
	
	// TODO what should leaves publish?
	public boolean hasValue() {
		/*if(results != null)
			return true;
		return false;*/
		return false;
	}
	public String getValue() {
		//return Utils.join(results, ", ");
		return null;
	}
	
	/**
	 * Returns a <code>String[]</code>.
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
			throws MissingVariableException, ExecutionFailure {
		try {
			Parser parser = (Parser) resource;
			PatternInterface pattern = parser.pattern.compile(variables, context.regexpInterface);
			String replacement = parser.replacement.compile(variables);
			return pattern.allMatches(stringToParse, replacement, minMatch, maxMatch);
		} catch(MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		} catch (InvalidRangeException e) {
			throw new ExecutionFailure(e);
		}
	}

	/**
	 * @return An array of {@link ScraperExecutableChild}s.
	 */
	protected Executable[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		results = (String[]) result;
		Vector scraperExecutions = new Vector();
		for(int i = 0 ; i < pipes.length ; i ++) {
			for(int j = 0 ; j < results.length ; j ++ ) {
				if(hasName()) {
					// If the Leaf has a Name, send a single match to spawn the scraper.
					scraperExecutions.add(
							new ScraperExecutableChild(context, pipes[i], this, variables, getName(), results[j]));
				} else {
					//TODO: should this spawn only one per pipe?
					// If the Leaf does not, spawn the scraper with the standard variables.
					new ScraperExecutableChild(context, pipes[i], this, variables);
				}
			}
		}
		Executable[] children = new ScraperExecutable[scraperExecutions.size()];
		scraperExecutions.copyInto(children);
		return children;
	}
}
