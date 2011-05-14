package net.microscraper.resources.definitions;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

/**
 * Class to make an HTTP POST request.
 * @see WebPage
 * @author realest
 *
 */
public class WebPagePost extends WebPageBody {
	private final Post[] posts;
	
	/**
	 * @param ref {@link Reference} A ref to uniquely identify the web page.
	 * @param url A URL to use. 
	 * @param headers An array of headers to add when requesting this web page.
	 * @param cookies An array of cookies to add to the browser before requesting this web page.
	 * @param priorWebPages An array of web pages that should be loaded before
	 * requesting this web page.
	 * @param terminates An array of regular expression resources that terminate loading.
	 * @param posts An array of posts to add to include in the request.
	 */
	public WebPagePost(Reference ref, URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPageHead[] webPages, Regexp[] terminates, Post[] posts) {
		super(ref, url, headers, cookies, webPages, terminates);
		this.posts = posts;
	}
	
	protected String getResponse(ExecutionContext context)
				throws ExecutionDelay, ExecutionFatality,
				DelayRequest, BrowserException {
		EncodedNameValuePair[] posts = generateEncodedNameValuePairs(context, this.posts);
		return context.getBrowser().post(generateURL(context), generateHeaders(context), generateCookies(context), generateTerminates(context), posts);
	}
}
