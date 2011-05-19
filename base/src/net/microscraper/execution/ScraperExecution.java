package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp;
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
import net.microscraper.model.Variable;
import net.microscraper.model.Link;
import net.microscraper.model.MustacheNameValuePair;
import net.microscraper.model.MustacheTemplate;
import net.microscraper.model.Page;
import net.microscraper.model.Parser;
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
public class ScraperExecution implements Execution, Variables, MustacheCompiler, HasLeafExecutions,
		HasVariableExecutions, HasScraperExecutions {
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
	
	private String source = null;
	private String missingVariable = null;
	private String lastMissingVariable = null;
	private Exception failure = null;
	
	/**
	 * @param scraper The {@link Scraper} that this {@link ScraperExecution} corresponds to.
	 * @param context
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	public ScraperExecution(Link pipe, Context context, UnencodedNameValuePair[] extraVariables) {
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
	
	public void run() {
		if(!isComplete() && !hasFailed() && !isStuck()) {
			try {
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
						PageExecution page = new PageExecution(context, this, context.loadPage(scraperSource.pageLinkSource));
						page.run();
						if(page.isComplete()) {
							source = page.getBody();
						}
					}
				}
				
				if(source != null) {
					variableExecutions = new VariableExecution[variables.length];
					for(int i = 0 ; i < variables.length ; i ++) {
						variableExecutions[i] = new VariableExecution(context, this, variables[i], source);
					}
					leafExecutions = new LeafExecution[leaves.length];
					for(int i = 0 ; i < leaves.length ; i ++) {
						leafExecutions[i] = new LeafExecution(context, this, this, leaves[i], source);
					}
					scraperExecutions = new ScraperExecution[pipes.length];
					for(int i = 0 ; i < pipes.length ; i ++) {
						scraperExecutions[i] = new ScraperExecutionChild(pipes[i], context, this);
					}
				}
			} catch(MissingVariableException e) {
				lastMissingVariable = missingVariable;
				missingVariable = e.name;
			} catch (MustacheTemplateException e) {
				failure = e;
			} catch (IOException e) {
				failure = e;
			} catch (DeserializationException e) {
				failure = e;
			}
		}
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
	
	public boolean isStuck() {
		if(lastMissingVariable != null & missingVariable != null)
			if(lastMissingVariable.equals(missingVariable))
				return true;
		return false;
	}
	
	public boolean hasFailed() {
		if(failure != null)
			return true;
		return false;
	}
	
	public boolean isComplete() {
		if(source != null)
			return true;
		return false;
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
}
