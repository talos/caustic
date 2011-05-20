package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Leaf;
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
public class ScraperExecution extends BasicExecution implements HasVariableExecutions, MustacheCompiler,
		HasLeafExecutions, HasScraperExecutions {
	private final Link pipe;
	private final Context context;	
	private final UnencodedNameValuePair[] extraVariables;	
	
	private Scraper scraper;
	
	private Variable[] variables;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	private Leaf[] leaves;
	private LeafExecution[] leafExecutions = new LeafExecution[0];
	
	private Link[] pipes;
	private ScraperExecution[] scraperExecutions = new ScraperExecution[0];
	
	private PageExecution pageSource = null;
	private String source = null;
	
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
	protected ScraperExecution(Link pipe, Context context, UnencodedNameValuePair[] extraVariables, HasVariableExecutions parent) {
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
		for(int i = 0 ; i < variables.length ; i ++) {
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
		for(int i = 0 ; i < variables.length ; i ++) {
			if(variableExecutions[i].containsKey(key)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean protectedRun() throws IOException, DeserializationException, MissingVariableException, MustacheTemplateException, DelayRequest, BrowserException, InvalidBodyMethodException, ScraperSourceException {
		if(scraper == null) {
			scraper = context.loadScraper(pipe);
			
			variables = scraper.getVariables();
			leaves = scraper.getLeaves();
			pipes = scraper.getPipes();
		}
		if(source == null) {
			ScraperSource scraperSource = scraper.scraperSource;
			if(scraperSource.hasStringSource) {
				source = this.compile(scraperSource.stringSource);
			} else {
				if(pageSource == null) {
					pageSource = new PageExecution(context, this, scraperSource.pageLinkSource);
				}
				pageSource.run();
				if(pageSource.isComplete()) {
					source = pageSource.getBody();
				} else if(pageSource.isStuck()) {
					throw new MissingVariableException(this, pageSource.stuckOn());
				} else if(pageSource.hasFailed()) {
					throw new ScraperSourceException(this, pageSource.failedBecause());
				}
			}
		}
		if(source != null) {
			variableExecutions = new VariableExecution[variables.length];
			for(int i = 0 ; i < variables.length ; i ++) {
				variableExecutions[i] = new VariableExecution(context, this, this, variables[i], source);
			}
			leafExecutions = new LeafExecution[leaves.length];
			for(int i = 0 ; i < leaves.length ; i ++) {
				leafExecutions[i] = new LeafExecution(context, this, this, leaves[i], source);
			}
			scraperExecutions = new ScraperExecution[pipes.length];
			for(int i = 0 ; i < pipes.length ; i ++) {
				scraperExecutions[i] = new ScraperExecutionChild(pipes[i], context, this);
			}
			return true;
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
	
	public Execution[] getChildren() {
		Execution[] children = new Execution[getVariableExecutions().length + getLeafExecutions().length + getScraperExecutions().length];
		for(int i = 0 ; i < getVariableExecutions().length ; i++) {
			children[i] = getVariableExecutions()[i];
		}
		for(int i = 0 ; i < getLeafExecutions().length ; i ++) {
			children[i + getVariableExecutions().length] = getLeafExecutions()[i];
		}
		for(int i = 0 ; i < getScraperExecutions().length ; i ++) {
			children[i + getVariableExecutions().length + getLeafExecutions().length] = getScraperExecutions()[i];
		}
		return children;
	}
	
	public VariableExecution[] getVariableExecutions() {
		return variableExecutions;
	}

	public LeafExecution[] getLeafExecutions() {
		return leafExecutions;
	}

	public ScraperExecution[] getScraperExecutions() {
		return scraperExecutions;
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
}
