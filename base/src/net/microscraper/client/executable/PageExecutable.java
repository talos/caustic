package net.microscraper.client.executable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Ref;
import net.microscraper.server.resource.MustacheEncodedNameValuePair;
import net.microscraper.server.resource.MustacheUnencodedNameValuePair;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Pattern;
import net.microscraper.server.resource.Resource;

/**
 * When {@link #run}, {@link PageExecutable} makes an HTTP request according to
 * the instructions linked at {@link #pageLink}.
 * @author john
 *
 */
public class PageExecutable extends BasicExecutable {
	
	/**
	 * A {@link Ref} to the {@link Page} that this {@link Executable} will load when {@link #run}.
	 */
	private final Ref pageLink;
	
	/**
	 * The {@link ScraperExecutable} that this {@link Page} is using as its source of
	 * {@link Variables} for {@link Mustache} compilation.
	 */
	private final ScraperExecutable enclosingScraper;
	
	/**
	 * The {@link Browser} that this {@link 
	 */
	private final Browser browser;
	
	private String body = null;
	
	public PageExecutable(ExecutionContext context, ScraperExecutable enclosingScraper, Ref pageLink) {
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
	
	public Executable[] children() {
		return new Executable[0];
	}
	
	protected Resource generateResource(ExecutionContext context)
				throws IOException, DeserializationException {
		return context.resourceLoader.loadPage(pageLink);
	}
	
	/**
	 * @return The body of the page, if the {@link PageExecutable}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
			throws MissingVariableException, BrowserDelayException, ExecutionFailure {
		try {
			Page page = (Page) resource;
			// Temporary executions to do before.  Not published, executed each time.
			for(int i = 0 ; i < page.loadBeforeLinks.length ; i ++) {
				PageExecutable pageBeforeExecution = new PageExecutable(context, enclosingScraper, page.loadBeforeLinks[i]);
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
	 * An empty array, {@link PageExecutable} does not have children.
	 */
	protected Executable[] generateChildren(ExecutionContext context, 
			Resource resource, Object result) {
		return new Executable[0];
	}
}
