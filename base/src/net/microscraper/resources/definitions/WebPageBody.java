package net.microscraper.resources.definitions;

import net.microscraper.client.Browser;

/**
 * Abstract class to obtain the body of a WebPage.
 * @author realest
 *
 */
public abstract class WebPageBody extends WebPage {
	private final Regexp[] terminates;
	protected WebPageBody(URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPage[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages);
		this.terminates = terminates;
	}

	/**
	 * Load the WebPage.  Terminates at the point that one of the terminates regular expressions
	 * matches the body.
	 * @param browser the browser to use.
	 * @return The loaded body.
	 */
	public abstract String loadUsing(Browser browser);
}
