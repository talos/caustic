package net.microscraper.client.interfaces;

import java.util.Date;

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
	
	/*
	public JSON.Object loadJSON(String url, Interfaces.JSON jsonInterface)
			throws InterruptedException, BrowserException, JSONInterfaceException;
	*/
	
	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from Set-Cookie to the Browser's cookie store.
	 * @param url the {@link URLInterface} to HTTP Head.
	 * @param headers Array of NameValuePair extra headers.
	 * @param cookies Array of NameValuePair extra cookies.  These should also be added to the browser's cookie store.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract void head(URLInterface url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies)
			throws BrowserDelayException, BrowserException;
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the {@link URLInterface} to HTTP Get.
	 * @param headers Array of NameValuePair extra headers.
	 * @param cookies Array of NameValuePair extra cookies.  These should also be added to the browser's cookie store.
	 * @param terminates Array of Patterns that prematurely terminate the load and return the body.
	 * @return the body of the response.
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String get(URLInterface url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies,
			PatternInterface[] terminates) throws BrowserDelayException, BrowserException;
	
	/**
	 * Make an HTTP Post request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the {@link URLInterface} to HTTP Get.
	 * @param headers Array of NameValuePair extra headers.
	 * @param cookies Array of NameValuePair extra cookies.  These should also be added to the browser's cookie store.
	 * @param terminates Array of Patterns that prematurely terminate the load and return the body.
	 * @param posts Array of NameValuePair post data.
	 * @return
	 * @throws BrowserDelayException if the request should be made again later to avoid overburdening the host.
	 * @throws BrowserException if there is an exception requesting the page.
	 */
	public abstract String post(URLInterface url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies, PatternInterface[] terminates, EncodedNameValuePair[] posts)
			throws BrowserDelayException, BrowserException;
}
