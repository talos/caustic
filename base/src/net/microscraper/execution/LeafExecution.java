package net.microscraper.execution;

import java.io.IOException;

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

public class LeafExecution extends ParsableExecution implements HasScraperExecutions {
	private String[] results = null;
	private final int minMatch;
	private final int maxMatch;
	private final MustacheCompiler mustache;
	private final String stringToParse;
	private final Link[] pipes;
	private final HasVariableExecutions callerVariables;
	private final Context context;
	private ScraperExecution[] scraperExecutions = new ScraperExecution[0];
	
	public LeafExecution(Context context, MustacheCompiler mustache, HasVariableExecutions callerVariables,
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

	protected boolean protectedRun() throws NoMatchesException, MissingGroupException,
					InvalidRangeException, MustacheTemplateException, MissingVariableException,
					IOException, DeserializationException {
		Interfaces.Regexp.Pattern pattern = mustache.compile(getParser().pattern);
		String replacement = mustache.compile(getParser().replacement);
		String[] output;
			output = pattern.allMatches(stringToParse, replacement, minMatch, maxMatch);

		scraperExecutions = new ScraperExecution[output.length * pipes.length];
		for(int i = 0 ; i < pipes.length ; i ++) {
			for(int j = 0 ; j < output.length ; j ++ ) {
				if(hasName()) {
					scraperExecutions[(i * j) + i] = new ScraperExecutionChild(pipes[i], context, callerVariables, getName(), output[j]);
				} else {
					scraperExecutions[(i * j) + i] = new ScraperExecutionChild(pipes[i], context, callerVariables);
				}
			}
		}
		return true;
	}
	
	
	public ScraperExecution[] getScraperExecutions() {
		return scraperExecutions;
	}
	
	public Execution[] getChildren() {
		return getScraperExecutions();
	}

	public boolean hasPublishValue() {
		if(results != null)
			return true;
		return false;
	}
	public String getPublishValue() {
		return Utils.join(results, ", ");
	}
}
