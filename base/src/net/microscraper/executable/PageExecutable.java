package net.microscraper.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.Mustache;
import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.regexp.PatternInterface;

/**
 * When {@link #run}, {@link PageExecutable} makes an HTTP request according to
 * the instructions from {@link Page}.
 * @author john
 *
 */
public class PageExecutable extends BasicExecutable {
	
	private final Variables extendedVariables;
	private final Browser browser;
	
	public PageExecutable(Interfaces interfaces,
			Page page, Variables extendedVariables, Result source) {
		super(interfaces, page, source);
		browser = interfaces.getBrowser();
		this.extendedVariables = extendedVariables;
	}
	
	private PatternInterface[] getStopBecause(Page page) throws MissingVariableException, MustacheTemplateException {
		PatternInterface[] stopPatterns = new PatternInterface[page.getStopBecause().length];
		for(int i  = 0 ; i < stopPatterns.length ; i++) {
			stopPatterns[i] = page.getStopBecause()[i]
					.compileWith(getInterfaces().getRegexpCompiler(), this);
		}
		return stopPatterns;
	}
	
	private String getURL() throws MissingVariableException, MustacheTemplateException {
		//return page.getTemplate().compile(this);
		Page page = (Page) getInstruction();
		return Mustache.compileEncoded(page.getTemplate().toString(), this, getInterfaces().getBrowser(), Browser.UTF_8);
	}
	
	private void head(Page page) throws UnsupportedEncodingException,
				MissingVariableException,
				BrowserException, MustacheTemplateException {
		browser.head(true, getURL(), 
				MustacheNameValuePair.compile(page.getHeaders(), this),
				MustacheNameValuePair.compile(page.getCookies(), this));
	}
	
	private String get(Page page) throws UnsupportedEncodingException,
				MissingVariableException,
				BrowserException, MustacheTemplateException {
		return browser.get(true, getURL(),
				MustacheNameValuePair.compile(page.getHeaders(), this),
				MustacheNameValuePair.compile(page.getCookies(), this),
				getStopBecause(page));
	}
	
	private String post(Page page) throws UnsupportedEncodingException,
				MissingVariableException,
				BrowserException, MustacheTemplateException {	
		return browser.post(true, getURL(),
				MustacheNameValuePair.compile(page.getHeaders(), this),
				MustacheNameValuePair.compile(page.getCookies(), this),
				getStopBecause(page),
				MustacheNameValuePair.compile(page.getPosts(), this));
	}
	
	/**
	 * @param page The {@link Page} {@link Instruction} whose {@link Page#method} should be executed.
	 * @return The body of the page, if the {@link PageExecutable}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	protected String doMethod(Page page) throws MissingVariableException, ExecutionFailure {
		try {
			// Temporary executions to do before.  Not published, executed each time.
			for(int i = 0 ; i < page.getPreload().length ; i ++) {
				doMethod((Page) page.getPreload()[i]);
			}
			if(page.getMethod().equals(Page.Method.GET)) {
				return get(page);
			} else if(page.getMethod().equals(Page.Method.POST)) {
				return post(page);
			} else if(page.getMethod().equals(Page.Method.HEAD)) {
				head(page);
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionFailure(e);
		} catch (MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (BrowserException e) {
			throw new ExecutionFailure(e);
		}
	}
	protected String[] generateResultValues() 
			throws MissingVariableException, MustacheTemplateException,
			ExecutionFailure {
		Page page = (Page) getInstruction();
		return new String[] { doMethod(page) };
	}

	public final String get(String key) throws MissingVariableException {
		if(isComplete()) {
			//Executable[] children = getChildren();
			for(int i = 0 ; i < getFindOneExecutableChildren().length ; i ++) {
				String localValue = getFindOneExecutableChildren()[i].localGet(key);
				if(localValue != null) {
					return localValue;
				}
			}
		}
		if(hasSource()) {
			//TODO: access to variables via mustache'd URI.  yay or nay?
			//if(getSource().hasName()) {
				if(getSource().getName().equals(key)) {
					return getSource().getValue();
				}
			//}
		}
		return extendedVariables.get(key);
	}
	
	public final boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
	}

	protected boolean generatesManyResults() {
		return false;
	}

	protected String getDefaultName() throws MustacheTemplateException,
			MissingVariableException {
		return getURL();
	}
}
