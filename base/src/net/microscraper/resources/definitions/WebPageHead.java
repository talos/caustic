package net.microscraper.resources.definitions;

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
	 * @param priorWebPages An array of web pages that should have HTTP Head run on them before
	 * requesting this web page.
	 */
	public WebPageHead(URL url, GenericHeader[] headers,
			Cookie[] cookies, WebPageHead[] webPages) {
		super(url, headers, cookies, webPages);
	}
}
