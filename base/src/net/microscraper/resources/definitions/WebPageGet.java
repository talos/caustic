package net.microscraper.resources.definitions;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

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

	protected String getResponse(ExecutionContext context)
			throws ExecutionDelay, ExecutionFatality, DelayRequest, BrowserException  {
		return context.getBrowser().get(generateURL(context), generateHeaders(context), generateCookies(context), generateTerminates(context));
	}
}
