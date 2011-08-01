package net.microscraper.interfaces.browser;

import net.microscraper.NameValuePair;
import net.microscraper.interfaces.regexp.PatternInterface;

/**
 * Implementations of the {@link Browser} interface can be used by to make HTTP requests and handle the responses.
 */
public interface Browser {
	public static final int TIMEOUT = 30000;
	public static final int MAX_REDIRECTS = 50;
	public static final int SUCCESS_CODE = 200;
	public static final int DEFAULT_MAX_KBPS_FROM_HOST = 30;
	public static final int DEFAULT_SLEEP_TIME = 500;
	
	public static final String LOCATION_HEADER_NAME = "location";
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public static final String USER_AGENT_HEADER_NAME = "User-Agent";
	public static final String USER_AGENT_HEADER_DEFAULT_VALUE = "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)";
	
	public static final String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";
	public static final String ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE = "en-US,en;q=0.8";
	
	public static final String ACCEPT_HEADER_NAME = "Accept";
	public static final String ACCEPT_HEADER_DEFAULT_VALUE = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	public static final String ACCEPT_HEADER_JSON_VALUE = "application/json,text/json";
	
	public static final String UTF_8 = "UTF-8";
	
	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from the Set-Cookie response header to the {@link Browser}'s cookie store.
	 * @param useRateLimit Whether to avoid overburdening a host.
	 * @param url the URL to HTTP Head.
	 * @param headers Array of {@link BasicNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract void head(boolean useRateLimit, String url, NameValuePair[] headers, NameValuePair[] cookies)
			throws BrowserException;
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param useRateLimit Whether to avoid overburdening a host.
	 * @param url the URL to HTTP Get.
	 * @param headers Array of {@link BasicNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @param terminates Array of {@link PatternInterface}s that prematurely terminate the load and return the body.  Can be <code>null</code> if there are none.
	 * @return The body of the response.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host, if <code>useRateLimit</code> is <code>true</code>.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String get(boolean useRateLimit, String url, NameValuePair[] headers, NameValuePair[] cookies,
			PatternInterface[] terminates) throws BrowserException;
	
	/**
	 * Make an HTTP Post request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param useRateLimit Whether to avoid overburdening a host.
	 * @param url the URL to HTTP Get.
	 * @param headers Array of {@link BasicNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @param terminates Array of {@link PatternInterface}s that prematurely terminate the load and return the body.  Can be <code>null</code> if there are none.
	 * @param posts Array of {@link EncodedNameValuePair} post data.  Can be <code>null</code> if there are none.
	 * @return The body of the response.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host, if <code>useRateLimit</code> is <code>true</code>.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String post(boolean useRateLimit, String url, NameValuePair[] headers,
			NameValuePair[] cookies, PatternInterface[] terminates, NameValuePair[] posts)
			throws BrowserException;
}
