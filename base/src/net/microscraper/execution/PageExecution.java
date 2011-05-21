package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.MustacheEncodedNameValuePair;
import net.microscraper.model.MustacheUnencodedNameValuePair;
import net.microscraper.model.Page;
import net.microscraper.model.Pattern;
import net.microscraper.model.Resource;

public class PageExecution extends BasicExecution {
	private final Link pageLink;
	private final ScraperExecution enclosingScraper;
	private final Browser browser;
	
	private String body = null;
	
	public PageExecution(ExecutionContext context, ScraperExecution enclosingScraper, Link pageLink) {
		super(context, pageLink.location, enclosingScraper);
		this.enclosingScraper = enclosingScraper;
		this.browser = context.browser;
		this.pageLink = pageLink;
	}
	
	private void head(ExecutionContext context, Page page) throws UnsupportedEncodingException, BrowserDelayException, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException {
		browser.head(page.url.compile(enclosingScraper),
				MustacheUnencodedNameValuePair.compile(page.headers, enclosingScraper),
				MustacheEncodedNameValuePair.compile(page.cookies, enclosingScraper, context.encoding));
	}
	
	private String get(ExecutionContext context, Page page) throws UnsupportedEncodingException, BrowserDelayException, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {
		return browser.get(page.url.compile(enclosingScraper),
				MustacheUnencodedNameValuePair.compile(page.headers, enclosingScraper),
				MustacheEncodedNameValuePair.compile(page.cookies, enclosingScraper, context.encoding),
				Pattern.compile(page.terminates, enclosingScraper, context.regexpInterface));
	}
	
	private String post(ExecutionContext context, Page page) throws UnsupportedEncodingException, BrowserDelayException, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {	
		return browser.post(page.url.compile(enclosingScraper),
				MustacheUnencodedNameValuePair.compile(page.headers, enclosingScraper),
				MustacheEncodedNameValuePair.compile(page.cookies, enclosingScraper, context.encoding),
				Pattern.compile(page.terminates, enclosingScraper, context.regexpInterface),
				MustacheEncodedNameValuePair.compile(page.posts, enclosingScraper, context.encoding));
	}
	
	public String getBody() {
		return body;
	}
	
	public Execution[] children() {
		return new Execution[0];
	}
/*
	public Execution[] getChildren() {
		return new Execution[0];
	}
*/
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

	protected Resource generateResource(ExecutionContext context)
				throws IOException, DeserializationException {
		return context.resourceLoader.loadPage(pageLink);
	}
	
	/**
	 * @return The body of the page, if the {@link PageExecution}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
			throws MissingVariableException, BrowserDelayException, ExecutionFailure {
		try {
			Page page = (Page) resource;
			// Temporary executions to do before.  Not published, executed each time.
			for(int i = 0 ; i < page.loadBeforeLinks.length ; i ++) {
				PageExecution pageBeforeExecution = new PageExecution(context, enclosingScraper, page.loadBeforeLinks[i]);
				Page pageBefore = (Page) pageBeforeExecution.generateResource(context);
				pageBeforeExecution.generateResult(context, pageBefore);
			}
			if(page.method.equals(Page.Method.GET)) {
				return get(context, page);
			} else if(page.method.equals(Page.Method.POST)) {
				return post(context, page);
			} else if(page.method.equals(Page.Method.HEAD)) {
				head(context, page);
				return null;
			} else {
				throw new InvalidBodyMethodException(page);
			}
		} catch(DeserializationException e) {
			throw new ExecutionFailure(e);
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionFailure(e);
		} catch (MalformedURLException e) {
			throw new ExecutionFailure(e);
		} catch (BrowserException e) {
			throw new ExecutionFailure(e);
		} catch (MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (InvalidBodyMethodException e) {
			throw new ExecutionFailure(e);
		} catch (IOException e) {
			throw new ExecutionFailure(e);
		}
	}

	/**
	 * An empty array, {@link PageExecution} does not have children.
	 */
	protected Execution[] generateChildren(ExecutionContext context, 
			Resource resource, Object result) {
		return new Execution[0];
	}
}
