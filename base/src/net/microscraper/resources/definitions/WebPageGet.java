package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

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
			WebPageHead[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages, terminates);
	}

	protected String getResponse(ExecutionContext context)
			throws ExecutionDelay, ExecutionFailure, ExecutionFatality,
			DelayRequest, BrowserException, MalformedURLException,
			UnsupportedEncodingException {
		return context.browser.get(generateURL(context), generateHeaders(context), generateCookies(context), generateTerminates(context));
	}
	
}
