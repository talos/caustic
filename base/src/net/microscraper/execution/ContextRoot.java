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
import net.microscraper.model.ExecutionLeaf;
import net.microscraper.model.ExecutionVariable;
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
 * that can be used for substitutions.  {@link ContextRoot} amasses as many variables (from
 * parsers) as possible, before branching into other Contexts at leaves.
 * @author realest
 *
 */
public class ContextRoot implements Variables {
	private final Scraper scraper;
	private final ResourceLoader resourceLoader;
	private final Browser browser;
	private final Log log;
	private final String encoding;
	private final Regexp regexp;
	private final Hashtable variables = new Hashtable();
	private final Vector childContexts = new Vector();

	/**
	 * @param scraper The {@link Scraper} that this {@link ContextRoot} corresponds to.
	 * @param resourceLoader The {@link ResourceLoader} this {@link ContextRoot} is set to use.
	 * @param browser The {@link Browser} this {@link ContextRoot} is set to use.
	 * @param log The {@link Log} this {@link ContextRoot} is set to use.
	 * @param encoding The encoding to use when encoding post data and cookies. "UTF-8" is recommended.
	 * @param regexp The {@link Regexp} interface to use when compiling regexps.
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to use as extra variables.
	 */
	public ContextRoot(Scraper scraper, ResourceLoader resourceLoader, Browser browser, Log log,
			String encoding, Regexp regexp, UnencodedNameValuePair[] extraVariables) {
		this.scraper = scraper;
		this.resourceLoader = resourceLoader;
		this.browser = browser;
		this.log = log;
		this.encoding = encoding;
		this.regexp = regexp;
		for(int i = 0 ; i < extraVariables.length ; i ++) {
			variables.put(extraVariables[i].getName(), extraVariables[i].getValue());
		}
	}

	public String get(String key) throws MissingVariableException {
		Object value = variables.get(key);
		if(value == null) {
			throw new MissingVariableException(this, key);
		} else {
			return (String) value;
		}
	}
	
	public boolean containsKey(String key) {
		return variables.containsKey(key);
	}
	
	/**
	 * Run the ContextRoot using its Scraper.
	 */
	public void run() {
		ExecutionVariable[] executionVariables = scraper.getVariables();
		ExecutionLeaf[] executionLeaves = scraper.getLeaves();
		
		// General piping, these Contexts are passed all this Context's variables once it
		// stops advancing, but nothing special.
		Link[] pipes = scraper.getPipes();
		for(int i = 0 ; i < pipes.length ; i ++) {
			try {
				addChild(resourceLoader.loadScraper(pipes[i]));
			} catch (IOException e) { // Warn that a scraper didn't load, but keep going.
				log.e(e);
			} catch (DeserializationException e) { // Warn that a scraper didn't load, but keep going.
				log.e(e);
			}
		}
		
		// Source string
		String source;
		ScraperSource scraperSource = scraper.scraperSource;
		if(scraperSource.hasStringSource) {
			source = compile(scraperSource.stringSource);
		} else {
			Page sourcePage = resourceLoader.loadPage(scraperSource.pageLinkSource);
			source = runBody(sourcePage);
		}
		
		// Run the executions til we can't run 'em no more.
		for(int i = 0 ; i < executionVariables.length ; i ++) {
			run(executionVariables[i], source);
		}
		for(int i = 0 ; i < executionLeaves.length ; i ++) {
			run(executionLeaves[i], source);
		}
		
		// Once we're exhausted, fire all the pipes
		for(int i = 0 ; i < childContexts.size() ; i ++) {
			((ContextChild) childContexts.elementAt(i)).run();
		}
	}
	
	private void runHead(Page page) throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException {
		browser.head(compile(page.url),
				compileUnencoded(page.headers),
				compileEncoded(page.cookies));
	}
	
	private String runBody(Page page) throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {
		if(page.method.equals(Page.Method.GET)) {
			return browser.get(compile(page.url),
						compileUnencoded(page.headers),
						compileEncoded(page.cookies),
						compile(page.terminates));
		} else if(page.method.equals(Page.Method.POST)) {
			return browser.post(compile(page.url),
					compileUnencoded(page.headers),
					compileEncoded(page.cookies),
					compile(page.terminates),
					compileEncoded(page.posts));
		} else {
			throw new InvalidBodyMethodException(page);
		}
	}
	
	private void run(ExecutionVariable variable, String stringToParse)// throws NoMatchesException, MissingGroupException, IOException, DeserializationException, MissingVariableException, MustacheTemplateException, InvalidRangeException {
	{
		Parser parser = resourceLoader.loadParser(variable.getParserLink());
		Interfaces.Regexp.Pattern pattern = compile(parser.pattern);
		String replacement = compile(parser.replacement);
		int matchNumber = variable.match;
		String output = pattern.match(stringToParse, replacement, matchNumber);
		
		// Only put it in the Variables hash if the user asked.
		if(variable.hasName()) {
			variables.put(variable.getName(), output);
		}
		
		ExecutionVariable[] childVariables = variable.getVariables();
		for(int i = 0 ; i < childVariables.length ; i ++) {
			run(childVariables[i], output);
		}
		
		ExecutionLeaf[] childLeaves = variable.getLeaves();
		for(int i = 0 ; i < childLeaves.length ; i ++) {
			try {
				run(childLeaves[i], output);
			} catch (IOException e) {
				log.e(e);
			} catch (DeserializationException e) {
				log.e(e);
			} catch (MissingVariableException e) {

				// TODO RETRY
			} catch (NoMatchesException e) {
				log.e(e);
			} catch (MissingGroupException e) {
				log.e(e);
			} catch (InvalidRangeException e) {
				log.e(e);
			} catch (MustacheTemplateException e) {
				log.e(e);
			}
		}
	}
	
	private void run(ExecutionLeaf leaf, String stringToParse) throws IOException, DeserializationException, MissingVariableException, MustacheTemplateException, NoMatchesException, MissingGroupException, InvalidRangeException {
		Parser parser = resourceLoader.loadParser(leaf.getParserLink());
		Interfaces.Regexp.Pattern pattern = compile(parser.pattern);
		String replacement = compile(parser.replacement);
		int minMatch = leaf.minMatch;
		int maxMatch = leaf.maxMatch;
		String[] output = pattern.allMatches(stringToParse, replacement, minMatch, maxMatch);
		
		Link[] pipes = leaf.getPipes();
		for(int i = 0 ; i < pipes.length ; i ++) {
			Scraper scraper = resourceLoader.loadScraper(pipes[i]);
			for(int j = 0 ; j < output.length ; j ++ ) {
				if(leaf.hasName()) {
					addChild(scraper, leaf.getName(), output[j]);
				} else {
					addChild(scraper);
				}
			}
		}
	}
	private String compile(MustacheTemplate template) throws MissingVariableException, MustacheTemplateException {
		return Mustache.compile(template.string, this);
	}

	private java.net.URL compile(net.microscraper.model.URL url) throws MalformedURLException, MissingVariableException, MustacheTemplateException {
		return new URL(compile(url.urlTemplate));
	}
	
	private Interfaces.Regexp.Pattern compile(Pattern uncompiledPattern) throws MissingVariableException, MustacheTemplateException {
		return regexp.compile(
				compile(uncompiledPattern.pattern),
				uncompiledPattern.isCaseInsensitive,
				uncompiledPattern.isMultiline,
				uncompiledPattern.doesDotMatchNewline);
	}
	
	private Interfaces.Regexp.Pattern[] compile(Pattern[] uncompiledPatterns) throws MissingVariableException, MustacheTemplateException {
		Interfaces.Regexp.Pattern[] patterns = new Interfaces.Regexp.Pattern[uncompiledPatterns.length];
		for(int i = 0 ; i < uncompiledPatterns.length ; i ++) {
			patterns[i] = compile(uncompiledPatterns[i]);
		}
		return patterns;
	}
	
	private EncodedNameValuePair[] compileEncoded(MustacheNameValuePair[] nameValuePairs)
				throws MissingVariableException, UnsupportedEncodingException, MustacheTemplateException {
		EncodedNameValuePair[] encodedNameValuePairs = 
			new EncodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new EncodedNameValuePair(
					compile(nameValuePairs[i].getName()),
					compile(nameValuePairs[i].getValue()),
					encoding);
		}
		return encodedNameValuePairs;
	}
	
	private UnencodedNameValuePair[] compileUnencoded(
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
	
	private void addChild(Scraper scraper) {
		childContexts.add(new ContextChild(scraper, resourceLoader,
				browser,log, encoding, regexp, this));
	}

	private void addChild(Scraper scraper, String name, String value) {
		childContexts.add(new ContextChild(scraper, 
				resourceLoader, browser,log, encoding, regexp, this,
				name, value));
	}
}
