package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
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
public class ScraperExecution extends BasicExecution implements Variables, MustacheCompiler {
	private final Link pipe;
	private final Context context;	
	private final UnencodedNameValuePair[] extraVariables;	
	
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	/**
	 * @param scraper The {@link Scraper} that this {@link ScraperExecution} corresponds to.
	 * @param context
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	public ScraperExecution(Link pipe, Context context, UnencodedNameValuePair[] extraVariables) {
		super(context, pipe.location);
		this.pipe = pipe;
		this.context = context;
		this.extraVariables = extraVariables;
	}

	/**
	 * @param scraper The {@link Scraper} that this {@link ScraperExecution} corresponds to.
	 * @param context
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	protected ScraperExecution(Link pipe, Context context, UnencodedNameValuePair[] extraVariables, Variables parent) {
		super(context, pipe.location, parent);
		this.pipe = pipe;
		this.context = context;
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
	
	public String compile(MustacheTemplate template) throws MissingVariableException, MustacheTemplateException {
		return Mustache.compile(template.string, this);
	}

	public java.net.URL compile(net.microscraper.model.URL url) throws MalformedURLException, MissingVariableException, MustacheTemplateException {
		return new URL(compile(url.urlTemplate));
	}
	
	public Interfaces.Regexp.Pattern compile(Pattern uncompiledPattern) throws MissingVariableException, MustacheTemplateException {
		return context.compile(
				compile(uncompiledPattern.pattern),
				uncompiledPattern.isCaseInsensitive,
				uncompiledPattern.isMultiline,
				uncompiledPattern.doesDotMatchNewline);
	}
	
	public Interfaces.Regexp.Pattern[] compile(Pattern[] uncompiledPatterns) throws MissingVariableException, MustacheTemplateException {
		Interfaces.Regexp.Pattern[] patterns = new Interfaces.Regexp.Pattern[uncompiledPatterns.length];
		for(int i = 0 ; i < uncompiledPatterns.length ; i ++) {
			patterns[i] = compile(uncompiledPatterns[i]);
		}
		return patterns;
	}
	
	public EncodedNameValuePair[] compileEncoded(MustacheNameValuePair[] nameValuePairs)
				throws MissingVariableException, UnsupportedEncodingException, MustacheTemplateException {
		EncodedNameValuePair[] encodedNameValuePairs = 
			new EncodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new EncodedNameValuePair(
					compile(nameValuePairs[i].getName()),
					compile(nameValuePairs[i].getValue()),
					context.getEncoding());
		}
		return encodedNameValuePairs;
	}
	
	public UnencodedNameValuePair[] compileUnencoded(
			MustacheNameValuePair[] nameValuePairs) throws MissingVariableException, MustacheTemplateException {
		UnencodedNameValuePair[] encodedNameValuePairs = 
			new UnencodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new UnencodedNameValuePair(
					compile(nameValuePairs[i].getName()),
					compile(nameValuePairs[i].getValue()));
		}
		return encodedNameValuePairs;
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

	protected Resource generateResource() throws IOException,
			DeserializationException {
		return context.loadScraper(pipe);
	}
	
	/**
	 * @return The source from which this {@link ScraperExecution}'s {@link ParsableExecution}'s will work.
	 */
	protected Object generateResult(Resource resource)
				throws MissingVariableException, BrowserDelayException,
				ExecutionFailure, MustacheTemplateException {
		try {
			Scraper scraper = (Scraper) resource;
			ScraperSource scraperSource = scraper.scraperSource;
			if(scraperSource.hasStringSource) {
				return this.compile(scraperSource.stringSource);
			} else {
				PageExecution sourcePageExecution = new PageExecution(context, this, scraperSource.pageLinkSource);
				
				Page sourcePage = (Page) sourcePageExecution.generateResource();
				return sourcePageExecution.generateResult(sourcePage);
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
	protected Execution[] generateChildren(Resource resource, Object result) {
		Scraper scraper = (Scraper) resource;
		String source = (String) result;
		
		Variable[] variables = scraper.getVariables();
		Leaf[] leaves = scraper.getLeaves();
		Link[] pipes = scraper.getPipes();
		
		Vector children = new Vector();
		Vector variableExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			VariableExecution variableExecution = new VariableExecution(context, this, this, variables[i], source);
			variableExecutions.add(variableExecution);
			children.add(variableExecution);
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			children.add(new LeafExecution(context, this, this, leaves[i], source));
		}
		for(int i = 0 ; i < pipes.length ; i ++) {
			children.add(new ScraperExecutionChild(pipes[i], context, this));
		}
		this.variableExecutions = new VariableExecution[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Execution[] childrenAry = new Execution[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
