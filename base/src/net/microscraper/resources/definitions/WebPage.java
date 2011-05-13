package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;

/**
 * Abstract class to request a web page using a browser.
 * @author realest
 *
 */
public abstract class WebPage implements Executable {
	protected final URL url;
	protected final GenericHeader[] headers;
	protected final Cookie[] cookies;
	protected final WebPage[] priorWebPages;

	protected WebPage(URL url, GenericHeader[] headers, Cookie[] cookies,
				WebPage[] priorWebPages) {
		this.url = url;
		this.headers = headers;
		this.cookies = cookies;
		this.priorWebPages = priorWebPages;
	}
}
