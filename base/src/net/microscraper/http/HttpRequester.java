package net.microscraper.http;

import java.io.IOException;
import java.util.Hashtable;

/**
 * An interface to make HTTP requests, returning {@link HttpResponse}.  Implementations should
 * not follow redirects automatically.
 * @author talos
 *
 */
public interface HttpRequester {

	/**
	 * The default number of seconds to wait before timing out on a
	 * request for {@link HttpBrowser} interfaces.
	 */
	public static final int DEFAULT_TIMEOUT_SECONDS = 30;

	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from response headers to the {@link HttpBrowser}'s cookie store.
	 * @param url the URL to HTTP Head.
	 * @param headers {@link Hashtable} of headers, mapping {@link String} to {@link String}.
	 * @return A {@link HttpResponse}.
	 * @throws IOException If there was an exception requesting the page.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract HttpResponse head(String url, Hashtable requestHeaders)
			throws IOException, InterruptedException;
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the URL to HTTP Get.
	 * @param headers {@link Hashtable} of headers, mapping {@link String} to {@link String}.
	 * @return A {@link HttpResponse}.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract HttpResponse get(String url, Hashtable requestHeaders) throws IOException, InterruptedException;
	
	/**
	 * Make an HTTP Post request with a {@link String} to encode into post data.
	 * This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the URL to HTTP Get.
	 * @param headers {@link Hashtable} of headers, mapping {@link String} to {@link String}.
	 * @param encodedPostData {@link String} of post data.  Should already be encoded.
	 * @return A {@link HttpResponse}.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract HttpResponse post(String url, Hashtable requestHeaders, String encodedPostData)
			throws IOException, InterruptedException;

	/**
	 * @param timeout How many seconds before giving up on a request.
	 */	
	public abstract void setTimeout(int timeoutSeconds);
	
}
