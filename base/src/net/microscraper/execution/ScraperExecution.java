package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Page;
import net.microscraper.model.Resource;
import net.microscraper.model.Variable;
import net.microscraper.model.Link;
import net.microscraper.model.MustacheNameValuePair;
import net.microscraper.model.MustacheTemplate;
import net.microscraper.model.Pattern;
import net.microscraper.model.Scraper;
import net.microscraper.model.ScraperSource;

/**
 * The context within which an executable is executed.  This contains a set of variables
 * that can be used for substitutions.  {@link ScraperExecution} amasses as many variables (from
 * parsers) as possible, before branching into other Contexts at leaves.
 * @author realest
 *
 */
public class ScraperExecution extends BasicExecution implements Variables {
	private final Link pipe;
	private final UnencodedNameValuePair[] extraVariables;	
	
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	/**
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	public ScraperExecution(ExecutionContext context, Link pipe,
				UnencodedNameValuePair[] extraVariables) {
		super(context, pipe.location);
		this.pipe = pipe;
		this.extraVariables = extraVariables;
	}

	/**
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	protected ScraperExecution(ExecutionContext context, Link pipe,
				UnencodedNameValuePair[] extraVariables, Execution parent) {
		super(context, pipe.location, parent);
		this.pipe = pipe;
		this.extraVariables = extraVariables;
	}

	public String get(String key) throws MissingVariableException {
		for(int i = 0 ; i < extraVariables.length ; i ++) {
			if(extraVariables[i].getName().equals(key)) {
				return extraVariables[i].getValue();
			}
		}
		for(int i = 0 ; i < variableExecutions.length ; i ++) {
			if(variableExecutions[i].containsKey(key)) {
				return variableExecutions[i].get(key);
			}
		}
		throw new MissingVariableException(this, key);
	}
	
	public boolean containsKey(String key) {
		for(int i = 0 ; i < extraVariables.length ; i ++) {
			if(extraVariables[i].getName().equals(key)) {
				return true;
			}
		}
		for(int i = 0 ; i < variableExecutions.length ; i ++) {
			if(variableExecutions[i].containsKey(key)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPublishName() {
		return false;
	}

	public String getPublishName() {
		return null;
	}

	public boolean hasPublishValue() {
		return false;
	}

	public String getPublishValue() {
		return null;
	}

	protected Resource generateResource(ExecutionContext context) throws IOException,
			DeserializationException {
		return context.resourceLoader.loadScraper(pipe);
	}
	
	/**
	 * @return The source from which this {@link ScraperExecution}'s {@link ParsableExecution}'s will work.
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
				throws MissingVariableException, BrowserDelayException,
				ExecutionFailure, MustacheTemplateException {
		try {
			Scraper scraper = (Scraper) resource;
			ScraperSource scraperSource = scraper.scraperSource;
			if(scraperSource.hasStringSource) {
				return scraperSource.stringSource.compile(this);
			} else {
				PageExecution sourcePageExecution = new PageExecution(context, this, scraperSource.pageLinkSource);
				
				Page sourcePage = (Page) sourcePageExecution.generateResource(context);
				return sourcePageExecution.generateResult(context, sourcePage);
			}
		} catch(DeserializationException e) {
			throw new ExecutionFailure(e);
		} catch (IOException e) {
			throw new ExecutionFailure(e);
		}
	}

	/**
	 * @return {@link VariableExecution}s, {@link LeafExecution}s, and {@link ScraperExecutionChild}s.
	 */
	protected Execution[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		Scraper scraper = (Scraper) resource;
		String source = (String) result;
		
		Variable[] variables = scraper.getVariables();
		Leaf[] leaves = scraper.getLeaves();
		Link[] pipes = scraper.getPipes();
		
		Vector children = new Vector();
		Vector variableExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			VariableExecution variableExecution = new VariableExecution(context, this,
					variables[i], source);
			variableExecutions.add(variableExecution);
			children.add(variableExecution);
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			children.add(new LeafExecution(context, this, this, leaves[i], source));
		}
		for(int i = 0 ; i < pipes.length ; i ++) {
			children.add(new ScraperExecutionChild(context, pipes[i], this, this));
		}
		this.variableExecutions = new VariableExecution[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Execution[] childrenAry = new Execution[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
