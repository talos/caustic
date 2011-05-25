package net.microscraper.client.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.client.interfaces.URLInterface;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.Page;

/**
 * When {@link #run}, {@link PageExecutable} makes an HTTP request according to
 * the instructions linked at {@link #pageLink}.
 * @author john
 *
 */
public class PageExecutable extends BasicExecutable {
	public PageExecutable(Interfaces context, Executable parent,
			Page page, Variables variables) {
		super(context, page, variables, parent);
	}
	
	private URLInterface getURL() throws NetInterfaceException, MissingVariableException, MustacheTemplateException {
		Page page = (Page) getResource();
		getContext().log.i(page.url.compile(getVariables()));
		return getContext().netInterface.getURL(page.url.compile(getVariables()));
	}
	
	private PatternInterface[] getStopBecause() {
		Page page = (Page) getResource();
		PatternInterface[] stopBecause = new PatternInterface[page.stopBecause.length];
		for(int i  = 0 ; i < stopBecause.length ; i++) {
			
		}
		return stopBecause;
		//RegexpExecutable.compile(page.stopBecause, getVariables(), context.regexpCompiler)
	}
	
	private void head() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				NetInterfaceException {
		Page page = (Page) getResource();
		Interfaces context = getContext();
		context.browser.head(getURL(), 
				MustacheUnencodedNameValuePair.compile(page.headers, getVariables()),
				MustacheEncodedNameValuePair.compile(page.cookies, getVariables(), context.encoding));
	}
	
	private String get() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				InvalidBodyMethodException, NetInterfaceException {
		Page page = (Page) getResource();
		Interfaces context = getContext();
		return context.browser.get(getURL(),
				MustacheUnencodedNameValuePair.compile(page.headers, getVariables()),
				MustacheEncodedNameValuePair.compile(page.cookies, getVariables(), context.encoding),
				getStopBecause());
	}
	
	private String post() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				InvalidBodyMethodException, NetInterfaceException {	
		Page page = (Page) getResource();
		Interfaces context = getContext();
		return context.browser.post(getURL(),
				MustacheUnencodedNameValuePair.compile(page.headers, getVariables()),
				MustacheEncodedNameValuePair.compile(page.cookies, getVariables(), context.encoding),
				getStopBecause(),
				MustacheEncodedNameValuePair.compile(page.posts, getVariables(), context.encoding));
	}
	
	/**
	 * @return The body of the page, if the {@link PageExecutable}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	protected Object generateResult()
			throws MissingVariableException, BrowserDelayException, ExecutionFailure {
		try {
			Page page = (Page) getResource();
			// Temporary executions to do before.  Not published, executed each time.
			for(int i = 0 ; i < page.preload.length ; i ++) {
				Page preloadPage = (Page) page.preload[i];
				
				PageExecutable preloadExecution =
					new PageExecutable(getContext(), this, preloadPage, getVariables());
				preloadExecution.generateResult();
			}
			if(page.method.equals(Page.Method.GET)) {
				return get();
			} else if(page.method.equals(Page.Method.POST)) {
				return post();
			} else if(page.method.equals(Page.Method.HEAD)) {
				head();
				return null;
			} else {
				throw new InvalidBodyMethodException(page);
			}
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionFailure(e);
		} catch (NetInterfaceException e) {
			throw new ExecutionFailure(e);
		} catch (BrowserException e) {
			throw new ExecutionFailure(e);
		} catch (MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (InvalidBodyMethodException e) {
			throw new ExecutionFailure(e);
		}
	}

	/**
	 * An empty array, {@link PageExecutable} does not have children.
	 */
	protected Executable[] generateChildren(Interfaces context, 
			Resource resource, Object result) {
		return new Executable[0];
	}
}
