package net.microscraper.execution;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.BrowserException;
import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
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
	private final MustacheCompiler mustache;
	private final String stringToParse;
	private final Link[] pipes;
	private final Variables callerVariables;
	private final Context context;
	//private ScraperExecution[] scraperExecutions = new ScraperExecution[0];
	
	public LeafExecution(Context context, MustacheCompiler mustache, Variables callerVariables,
			Leaf leaf, String stringToParse) {
		super(context, leaf, callerVariables);
		this.context = context;
		this.mustache = mustache;
		this.stringToParse = stringToParse;
		this.maxMatch = leaf.maxMatch;
		this.minMatch = leaf.minMatch;
		this.pipes = leaf.getPipes();
		this.callerVariables = callerVariables;
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

	protected Object generateResult(Resource resource)
			throws NoMatchesException, MissingGroupException,
			InvalidRangeException, MustacheTemplateException,
			MissingVariableException, IOException, DeserializationException,
			BrowserDelayException, BrowserException,
			InvalidBodyMethodException, ScraperSourceException {
		Parser parser = (Parser) resource;
		Interfaces.Regexp.Pattern pattern = mustache.compile(parser.pattern);
		String replacement = mustache.compile(parser.replacement);
		// returns String[]
		return pattern.allMatches(stringToParse, replacement, minMatch, maxMatch);
	}

	protected Execution[] generateChildren(Resource resource, Object result)
			throws NoMatchesException, MissingGroupException,
			InvalidRangeException, MustacheTemplateException,
			MissingVariableException, IOException, DeserializationException,
			BrowserDelayException, BrowserException,
			InvalidBodyMethodException, ScraperSourceException {
		results = (String[]) result;
		Vector scraperExecutions = new Vector();
		for(int i = 0 ; i < pipes.length ; i ++) {
			for(int j = 0 ; j < results.length ; j ++ ) {
				if(hasName()) {
					// If the Leaf has a Name, send a single match to spawn the scraper.
					scraperExecutions.add(
							new ScraperExecutionChild(pipes[i], context, callerVariables, getName(), results[j]));
				} else {
					// If the Leaf does not, spawn the scraper with the standard variables.
					scraperExecutions.add(
							new ScraperExecutionChild(pipes[i], context, callerVariables));
				}
			}
		}
		Execution[] children = new ScraperExecution[scraperExecutions.size()];
		scraperExecutions.copyInto(children);
		return children;
	}
}
