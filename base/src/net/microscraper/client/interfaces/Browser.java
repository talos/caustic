package net.microscraper.client.interfaces;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.UnencodedNameValuePair;

/**
 * Implementations of the Browser interface can be used by a microscraper Client
 * to load pages.
 */
public interface Browser {
	public static final int TIMEOUT = 30000;
	public static final int MAX_REDIRECTS = 50;
	public static final int SUCCESS_CODE = 200;
	public static final int MAX_KBPS_FROM_HOST = 30;
	
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
	 * @param useRateLimit Whether to throw a {@link BrowserDelayException} to avoid overburdening a host.
	 * @param url the {@link URLInterface} to HTTP Head.
	 * @param headers Array of {@link UnencodedNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host, if <code>useRateLimit</code> is <code>true</code>.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract void head(boolean useRateLimit, URLInterface url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies)
			throws BrowserDelayException, BrowserException;
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param useRateLimit Whether to throw a {@link BrowserDelayException} to avoid overburdening a host.
	 * @param url the {@link URLInterface} to HTTP Get.
	 * @param headers Array of {@link UnencodedNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @param terminates Array of {@link PatternInterface}s that prematurely terminate the load and return the body.  Can be <code>null</code> if there are none.
	 * @return The body of the response.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host, if <code>useRateLimit</code> is <code>true</code>.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String get(boolean useRateLimit, URLInterface url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies,
			PatternInterface[] terminates) throws BrowserDelayException, BrowserException;
	
	/**
	 * Make an HTTP Post request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param useRateLimit Whether to throw a {@link BrowserDelayException} to avoid overburdening a host.
	 * @param url the {@link URLInterface} to HTTP Get.
	 * @param headers Array of {@link UnencodedNameValuePair} extra headers.  Can be <code>null</code> if there are none.
	 * @param cookies Array of {@link EncodedNameValuePair} extra cookies.  These should also be added to the browser's cookie store.  Can be <code>null</code> if there are none.
	 * @param terminates Array of {@link PatternInterface}s that prematurely terminate the load and return the body.  Can be <code>null</code> if there are none.
	 * @param posts Array of {@link EncodedNameValuePair} post data.  Can be <code>null</code> if there are none.
	 * @return The body of the response.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host, if <code>useRateLimit</code> is <code>true</code>.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String post(boolean useRateLimit, URLInterface url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies, PatternInterface[] terminates, EncodedNameValuePair[] posts)
			throws BrowserDelayException, BrowserException;
}
