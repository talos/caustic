package net.microscraper.execution;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.model.Page;

public class PageExecution implements Execution {
	private final Page page;
	private final MustacheCompiler mustache;
	private final Browser browser;
	
	private String body = null;
	private boolean runSuccessfully = false;
	private DelayRequest delayRequest;
	
	private String missingVariable = null;
	private Exception failure = null;
	
	public PageExecution(Browser browser, MustacheCompiler mustache, Page page) {
		this.mustache = mustache;
		this.browser = browser;
		this.page = page;
	}
	
	private void head() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException {
		browser.head(mustache.compile(page.url),
				mustache.compileUnencoded(page.headers),
				mustache.compileEncoded(page.cookies));
	}
	
	private String get() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {
		return browser.get(mustache.compile(page.url),
				mustache.compileUnencoded(page.headers),
				mustache.compileEncoded(page.cookies),
				mustache.compile(page.terminates));
	}
	
	private String post() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {	
		return browser.post(mustache.compile(page.url),
				mustache.compileUnencoded(page.headers),
				mustache.compileEncoded(page.cookies),
				mustache.compile(page.terminates),
				mustache.compileEncoded(page.posts));
	}

	public void run() {
		if(runSuccessfully == false && !hasFailed()) {
			try {
				delayRequest = null;
				if(page.method.equals(Page.Method.GET)) {
					body = get();
					runSuccessfully = true;
				} else if(page.method.equals(Page.Method.POST)) {
					body = post();
					runSuccessfully = true;
				} else if(page.method.equals(Page.Method.HEAD)) {
					head();
					runSuccessfully = true;
				} else {
					failure = new InvalidBodyMethodException(page);
				}
			} catch(MissingVariableException e) {
				missingVariable = e.name;
			} catch(BrowserException e) {
				failure = e;
			} catch(DelayRequest e) {
				delayRequest = e;
			} catch(MustacheTemplateException e) {
				failure = e;
			} catch(InvalidBodyMethodException e) {
				failure = e;
			} catch(MalformedURLException e) {
				failure = e;
			} catch(UnsupportedEncodingException e) {
				failure = e;
			}
		}
	}

	public boolean isStuck() {
		return false;
	}

	public boolean hasFailed() {
		if(failure != null)
			return true;
		return false;
	}

	public boolean isComplete() {
		if(runSuccessfully == true)
			return true;
		return false;
	}
	
	public String getBody() {
		return body;
	}

	public Execution[] children() {
		return new Execution[0];
	}
}
