package net.microscraper.client.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.client.interfaces.URLInterface;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.Page;

/**
 * When {@link #run}, {@link PageExecution} makes an HTTP request according to
 * the instructions linked at {@link #pageLink}.
 * @author john
 *
 */
public class PageExecution {
	private final Page page;
	private final Interfaces interfaces;
	private final Variables variables;
	public PageExecution(Interfaces interfaces,
			Page page, Variables variables) {
		this.page = page;
		this.variables = variables;
		this.interfaces = interfaces;
		//super(context, page, variables, parent);
	}
	
	private URLInterface getURL() throws NetInterfaceException, MissingVariableException, MustacheTemplateException {
		return interfaces.netInterface.getURL(page.url.compile(variables));
	}
	
	private PatternInterface[] getStopBecause() throws MissingVariableException, MustacheTemplateException {
		PatternInterface[] stopPatterns = new PatternInterface[page.stopBecause.length];
		for(int i  = 0 ; i < stopPatterns.length ; i++) {
			stopPatterns[i] = new RegexpExecution(interfaces, page.stopBecause[i], variables).getPattern();
		}
		return stopPatterns;
		//RegexpExecutable.compile(page.stopBecause, getVariables(), context.regexpCompiler)
	}
	
	private void head() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				NetInterfaceException {
		interfaces.browser.head(getURL(), 
				MustacheUnencodedNameValuePair.compile(page.headers, variables),
				MustacheEncodedNameValuePair.compile(page.cookies, variables, interfaces.encoding));
	}
	
	private String get() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				InvalidBodyMethodException, NetInterfaceException {
		return interfaces.browser.get(getURL(),
				MustacheUnencodedNameValuePair.compile(page.headers, variables),
				MustacheEncodedNameValuePair.compile(page.cookies, variables, interfaces.encoding),
				getStopBecause());
	}
	
	private String post() throws UnsupportedEncodingException,
				BrowserDelayException, MissingVariableException,
				BrowserException, MustacheTemplateException,
				InvalidBodyMethodException, NetInterfaceException {	
		return interfaces.browser.post(getURL(),
				MustacheUnencodedNameValuePair.compile(page.headers, variables),
				MustacheEncodedNameValuePair.compile(page.cookies, variables, interfaces.encoding),
				getStopBecause(),
				MustacheEncodedNameValuePair.compile(page.posts, variables, interfaces.encoding));
	}
	
	/**
	 * @return The body of the page, if the {@link PageExecution}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	protected String getBody()
			throws MissingVariableException, BrowserDelayException, ExecutionFailure {
		try {
			// Temporary executions to do before.  Not published, executed each time.
			for(int i = 0 ; i < page.preload.length ; i ++) {
				Page preloadPage = (Page) page.preload[i];
				
				PageExecution preloadExecution =
					new PageExecution(interfaces, preloadPage, variables);
				preloadExecution.getBody();
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
	 * An empty array, {@link PageExecution} does not have children.
	 */
	protected Executable[] generateChildren(Interfaces context, 
			Resource resource, Object result) {
		return new Executable[0];
	}
}
