package net.microscraper.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.Page;

public class PageExecution extends BasicExecution {
	private final Link pageLink;
	private final ScraperExecution enclosingScraper;
	private final Context context;
	
	private String body = null;
	
	public PageExecution(Context context, ScraperExecution enclosingScraper, Link pageLink) {
		super(context, pageLink.location, enclosingScraper);
		this.enclosingScraper = enclosingScraper;
		this.context = context;
		this.pageLink = pageLink;
	}
	
	private void head(Page page) throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException {
		context.head(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies));
	}
	
	private String get(Page page) throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {
		return context.get(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies),
				enclosingScraper.compile(page.terminates));
	}
	
	private String post(Page page) throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {	
		return context.post(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies),
				enclosingScraper.compile(page.terminates),
				enclosingScraper.compileEncoded(page.posts));
	}

	protected boolean protectedRun() throws DelayRequest, MissingVariableException,
				BrowserException, MustacheTemplateException, InvalidBodyMethodException,
				IOException, DeserializationException {
		Page page = context.loadPage(pageLink);
		
		// Temporary executions to do before.  Not published, executed each time.
		for(int i = 0 ; i < page.loadBeforeLinks.length ; i ++) {
			new PageExecution(context, enclosingScraper, page.loadBeforeLinks[i]).protectedRun();
		}
		if(page.method.equals(Page.Method.GET)) {
			body = get(page);
		} else if(page.method.equals(Page.Method.POST)) {
			body = post(page);
		} else if(page.method.equals(Page.Method.HEAD)) {
			head(page);
		} else {
			throw new InvalidBodyMethodException(page);
		}
		return true;
	}

	public String getBody() {
		return body;
	}

	public Execution[] children() {
		return new Execution[0];
	}

	public Execution[] getChildren() {
		return new Execution[0];
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
