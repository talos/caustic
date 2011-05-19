package net.microscraper.execution;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.model.Page;
import net.microscraper.model.Resource;

public class PageExecution implements Execution {
	private final Page page;
	private final ScraperExecution enclosingScraper;
	private final Browser browser;
	
	private String body = null;
	private boolean runSuccessfully = false;
	private DelayRequest delayRequest;
	
	private String lastMissingVariable = null;
	private String missingVariable = null;
	private Exception failure = null;
	
	private final int id;
	private static int count = 0;
	
	public PageExecution(Browser browser, ScraperExecution enclosingScraper, Page page) {
		this.id = count;
		count++;
		this.enclosingScraper = enclosingScraper;
		this.browser = browser;
		this.page = page;
	}
	
	private void head() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException {
		browser.head(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies));
	}
	
	private String get() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {
		return browser.get(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies),
				enclosingScraper.compile(page.terminates));
	}
	
	private String post() throws UnsupportedEncodingException, DelayRequest, MissingVariableException, BrowserException, MalformedURLException, MustacheTemplateException, InvalidBodyMethodException {	
		return browser.post(enclosingScraper.compile(page.url),
				enclosingScraper.compileUnencoded(page.headers),
				enclosingScraper.compileEncoded(page.cookies),
				enclosingScraper.compile(page.terminates),
				enclosingScraper.compileEncoded(page.posts));
	}

	public void run() {
		if(runSuccessfully == false && !hasFailed()) {
			/*for(int i = 0 ; i < page.loadBefore.length ; i ++) {
				page.loadBefore[i].run();
			}*/
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
				lastMissingVariable = missingVariable;
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
		if(runSuccessfully == false) {
			if(lastMissingVariable != null && missingVariable != null) {
				if(lastMissingVariable.equals(missingVariable))
					return true;
			}
		}
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

	public Execution[] getChildren() {
		return new Execution[0];
	}

	public Resource getResource() {
		return page;
	}

	public Execution getCaller() {
		return enclosingScraper;
	}
	public boolean hasCaller() {
		return true;
	}

	public int getId() {
		return id;
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
