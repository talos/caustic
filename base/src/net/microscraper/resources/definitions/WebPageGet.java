package net.microscraper.resources.definitions;

import net.microscraper.client.Browser;
import net.microscraper.resources.ExecutionContext;

/**
 * Class to make an HTTP GET request.
 * @see WebPage
 * @author realest
 */
public final class WebPageGet extends WebPageBody {
	/**
	 * @param url A URL to use. 
	 * @param headers An array of headers to add when requesting this web page.
	 * @param cookies An array of cookies to add to the browser before requesting this web page.
	 * @param priorWebPages An array of web pages that should be loaded before
	 * requesting this web page.
	 * @param terminates An array of regular expression resources that terminate loading.
	 */
	public WebPageGet(URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPage[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages, terminates);
	}
	
	public String loadUsing(Browser browser) {
		return null;
	}
	
	public Object execute(ExecutionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
