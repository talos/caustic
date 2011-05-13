package net.microscraper.resources.definitions;

import net.microscraper.client.Browser;
import net.microscraper.resources.ExecutionContext;

/**
 * Class to 
 * @author realest
 *
 */
public final class WebPageHead extends WebPage {
	/**
	 * @param url A URL to use. 
	 * @param headers An array of headers to add when requesting this web page.
	 * @param cookies An array of cookies to add to the browser before requesting this web page.
	 * @param priorWebPages An array of web pages that should be loaded before
	 * requesting this web page.
	 */
	public WebPageHead(URL url, GenericHeader[] headers,
			Cookie[] cookies, WebPage[] webPages) {
		super(url, headers, cookies, webPages);
	}
	
	/**
	 * Send an HTTP Head for the web page.  This will add cookies to the browser.
	 * @param browser the browser to use.
	 */
	public void loadUsing(Browser browser) {
		
	}
	
	public Object execute(ExecutionContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
