package net.microscraper.client.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Leaf;
import net.microscraper.server.resource.Link;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Resource;
import net.microscraper.server.resource.Scraper;
import net.microscraper.server.resource.ScraperSource;
import net.microscraper.server.resource.Variable;

/**
 * {@link ScraperExecutable} is the {@link Executable} spawned by a {@link Scraper}.
 * When {@link #run}, it spawns a variety of other {@link Executable}s.
 * @author john
 *
 */
public class ScraperExecutable extends BasicExecutable implements Variables {
	private final Link pipe;
	private final UnencodedNameValuePair[] extraVariables;	
	
	private VariableExecutable[] variableExecutions = new VariableExecutable[0];
	
	/**
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	public ScraperExecutable(ExecutionContext context, Link pipe,
				UnencodedNameValuePair[] extraVariables) {
		super(context, pipe.location);
		this.pipe = pipe;
		this.extraVariables = extraVariables;
	}

	/**
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	protected ScraperExecutable(ExecutionContext context, Link pipe,
				UnencodedNameValuePair[] extraVariables, Executable parent) {
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
	
	protected Resource generateResource(ExecutionContext context) throws IOException,
			DeserializationException {
		return context.resourceLoader.loadScraper(pipe);
	}
	
	/**
	 * @return The source from which this {@link ScraperExecutable}'s {@link ParsableExecutable}'s will work.
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
				PageExecutable sourcePageExecution = new PageExecutable(context, this, scraperSource.pageLinkSource);
				
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
	 * @return {@link VariableExecutable}s, {@link LeafExecutable}s, and {@link ScraperExecutableChild}s.
	 */
	protected Executable[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		Scraper scraper = (Scraper) resource;
		String source = (String) result;
		
		Variable[] variables = scraper.getVariables();
		Leaf[] leaves = scraper.getLeaves();
		Link[] pipes = scraper.getPipes();
		
		Vector children = new Vector();
		Vector variableExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			VariableExecutable variableExecution = new VariableExecutable(context, this,
					variables[i], source);
			variableExecutions.add(variableExecution);
			children.add(variableExecution);
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			children.add(new LeafExecutable(context, this, this, leaves[i], source));
		}
		for(int i = 0 ; i < pipes.length ; i ++) {
			children.add(new ScraperExecutableChild(context, pipes[i], this, this));
		}
		this.variableExecutions = new VariableExecutable[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Executable[] childrenAry = new Executable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
