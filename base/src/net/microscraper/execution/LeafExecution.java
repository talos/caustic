package net.microscraper.execution;

import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Link;

public class LeafExecution extends ParsableExecution implements HasScraperExecutions {
	private String[] results = null;
	private final int minMatch;
	private final int maxMatch;
	private final MustacheCompiler mustache;
	private final String stringToParse;
	private final Link[] pipes;
	private final Variables variables;
	private final Context context;
	private ScraperExecution[] scraperExecutions = new ScraperExecution[0];
	
	private String missingVariable;
	private String lastMissingVariable;
	private Exception failure;
	
	public LeafExecution(Context context, MustacheCompiler mustache, Variables variables, Leaf leaf, String stringToParse) {
		super(context, leaf);
		this.context = context;
		this.mustache = mustache;
		this.stringToParse = stringToParse;
		this.maxMatch = leaf.maxMatch;
		this.minMatch = leaf.minMatch;
		this.pipes = leaf.getPipes();
		this.variables = variables;
	}

	public void run() {
		if(results == null && !hasFailed()) {
			try {
				Interfaces.Regexp.Pattern pattern = mustache.compile(getParser().pattern);
				String replacement = mustache.compile(getParser().replacement);
				String[] output;
					output = pattern.allMatches(stringToParse, replacement, minMatch, maxMatch);
	
				scraperExecutions = new ScraperExecution[output.length * pipes.length];
				for(int i = 0 ; i < pipes.length ; i ++) {
					for(int j = 0 ; j < output.length ; j ++ ) {
						if(hasName()) {
							scraperExecutions[(i * j) + i] = new ScraperExecutionChild(pipes[i], context, variables, getName(), output[j]);
						} else {
							scraperExecutions[(i * j) + i] = new ScraperExecutionChild(pipes[i], context, variables);
						}
					}
				}
			} catch (NoMatchesException e) {
				failure = e;
			} catch (MissingGroupException e) {
				failure = e;
			} catch (InvalidRangeException e) {
				failure = e;
			} catch (MustacheTemplateException e) {
				failure = e;
			} catch (MissingVariableException e) {
				lastMissingVariable = missingVariable;
				missingVariable = e.name;
			}
		}
	}
	
	public boolean isStuck() {
		if(!isComplete() && !hasFailed() && lastMissingVariable != null && missingVariable != null
				&& lastMissingVariable.equals(missingVariable))
			return true;
		return false;
	}

	public boolean isComplete() {
		if(results != null)
			return true;
		return false;
	}
	
	public boolean hasFailed() {
		if(super.hasFailed()) 
			return true;
		if(failure != null)
			return true;
		return false;
	}

	public ScraperExecution[] getScraperExecutions() {
		return scraperExecutions;
	}
}
