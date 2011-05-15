package net.microscraper.resources.definitions;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

/**
 * Class to make an HTTP GET request.
 * @see WebPage
 * @author realest
 */
public final class WebPageGet extends WebPageBody {
	/**
	 * @param url A URL to use. 
	 * @param ref {@link Reference} A ref to uniquely identify the web page.
	 * @param headers An array of headers to add when requesting this web page.
	 * @param cookies An array of cookies to add to the browser before requesting this web page.
	 * @param priorWebPages An array of web pages that should be loaded before
	 * requesting this web page.
	 * @param terminates An array of regular expression resources that terminate loading.
	 */
	public WebPageGet(Reference ref, URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPageHead[] priorWebPages, Regexp[] terminates) {
		super(ref, url, headers, cookies, priorWebPages, terminates);
	}

	protected String getResponse(Scraper context)
			throws ScrapingDelay, ScrapingFatality, DelayRequest, BrowserException  {
		return context.getBrowser().get(generateURL(context), generateHeaders(context), generateCookies(context), generateTerminates(context));
	}
}
