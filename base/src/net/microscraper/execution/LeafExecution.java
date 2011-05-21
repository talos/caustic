package net.microscraper.execution;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.BrowserException;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.Utils;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Link;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;

public class LeafExecution extends ParsableExecution {
	private String[] results = null;
	private final int minMatch;
	private final int maxMatch;
	private final String stringToParse;
	private final Link[] pipes;
	private final Variables variables;
	//private final Variables callerVariables;
	//private ScraperExecution[] scraperExecutions = new ScraperExecution[0];
	
	public LeafExecution(ExecutionContext context,
			Execution parent, Variables variables,
			Leaf leaf, String stringToParse) {
		super(context, leaf, parent);
		this.stringToParse = stringToParse;
		this.maxMatch = leaf.maxMatch;
		this.minMatch = leaf.minMatch;
		this.variables = variables;
		this.pipes = leaf.getPipes();
		//this.callerVariables = callerVariables;
	}
	
	public boolean hasPublishValue() {
		/*if(results != null)
			return true;
		return false;*/
		return false;
	}
	public String getPublishValue() {
		//return Utils.join(results, ", ");
		return null;
	}
	
	/**
	 * Returns a <code>String[]</code>
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
			throws MissingVariableException, ExecutionFailure {
		try {
			Parser parser = (Parser) resource;
			Interfaces.Regexp.Pattern pattern = parser.pattern.compile(variables, context.regexpInterface);
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
	 * @return An array of {@link ScraperExecutionChild}s.
	 */
	protected Execution[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		results = (String[]) result;
		Vector scraperExecutions = new Vector();
		for(int i = 0 ; i < pipes.length ; i ++) {
			for(int j = 0 ; j < results.length ; j ++ ) {
				if(hasName()) {
					// If the Leaf has a Name, send a single match to spawn the scraper.
					scraperExecutions.add(
							new ScraperExecutionChild(context, pipes[i], this, variables, getName(), results[j]));
				} else {
					//TODO: should this spawn only one per pipe?
					// If the Leaf does not, spawn the scraper with the standard variables.
					new ScraperExecutionChild(context, pipes[i], this, variables);
				}
			}
		}
		Execution[] children = new ScraperExecution[scraperExecutions.size()];
		scraperExecutions.copyInto(children);
		return children;
	}
}
