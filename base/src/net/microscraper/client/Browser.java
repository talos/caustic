package net.microscraper.client;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.regexp.Pattern;

/**
 * Implementations of the {@link Browser} interface can be used by to make HTTP requests and handle the responses.
 */
public interface Browser extends Loggable {
	/**
	 * The default number of seconds to wait before timing out on a
	 * request for {@link Browser} interfaces.
	 */
	public static final int DEFAULT_TIMEOUT = 30;
	
	/**
	 * The default number of redirects {@link Browser} interfaces will follow.
	 */
	public static final int MAX_REDIRECTS = 50;
	
	public static final int SUCCESS_CODE = 200;
	
	/**
	 * The default rate limit a {@link Browser} interface imposes upon itself for
	 * a single host.
	 */
	public static final int DEFAULT_RATE_LIMIT = 30;
	
	/**
	 * How many milliseconds a {@link Browser} interface will sleep before
	 * considering trying a host again, if when it last considered requesting
	 * from a host it would have exceeded its rate limit.
	 */
	public static final int DEFAULT_SLEEP_TIME = 500;
	
	/**
	 * How many kilobytes of a response a {@link Browser} should allow before 
	 * automatically terminating the request.
	 */
	public static final int DEFAULT_MAX_RESPONSE_SIZE = 2 * 1024;
	
	public static final String LOCATION_HEADER_NAME = "location";
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public static final String USER_AGENT_HEADER_NAME = "User-Agent";
	public static final String USER_AGENT_HEADER_DEFAULT_VALUE = "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)";
	
	public static final String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";
	public static final String ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE = "en-US,en;q=0.8";
	
	public static final String ACCEPT_HEADER_NAME = "Accept";
	public static final String ACCEPT_HEADER_DEFAULT_VALUE = "*/*";
	//public static final String ACCEPT_HEADER_JSON_VALUE = "application/json,text/json";
	
	public static final String UTF_8 = "UTF-8";

	public static final String GET = "get";
	public static final String POST = "post";
	public static final String HEAD = "head";
	
	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from the Set-Cookie response header to the {@link Browser}'s cookie store.
	 * @param url the URL to HTTP Head.
	 * @param headers {@link Hashtable} extra headers.
	 * @throws IOException If there was an exception requesting the page.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract void head(String url, Hashtable headers)
			throws IOException, InterruptedException;
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @return The body of the response.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract String get(String url, Hashtable headers, Pattern[] terminates) throws IOException, InterruptedException;
	
	/**
	 * Make an HTTP Post request with an array of {@link NameValuePair}s to encode into post data.
	 * This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @param posts {@link Hashtable} of post data.  Should be form encoded as name-value pairs.
	 * @return The body of the response.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract String post(String url, Hashtable headers, Pattern[] terminates, Hashtable posts)
			throws IOException, InterruptedException;

	/**
	 * Make an HTTP Post request with a {@link String} to encode into post data.
	 * This returns the body of the response, and adds cookies to the cookie jar.
	 * @param url the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @param postData {@link String} of post data.  Should be form encoded.
	 * @return The body of the response.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public abstract String post(String url, Hashtable headers, Pattern[] terminates, String postData)
			throws IOException, InterruptedException;
	
	/**
	 * Add an array of {@link Cookie}s to the {@link Browser}'s cookie store.
	 * @param cookies
	 */
	public abstract void addCookies(Cookie[] cookies);
	
	/**
	 * Set the rate limit for loading from a single host.  The {@link Browser} will wait until the rate is below
	 * this threshold before making another request.  Set this to <code>0</code> to disable rate limiting.
	 * @param rateLimitKBPS The rate limit to set, in kilobytes per second.
	 * @see #getRateLimit()
	 */
	public abstract void setRateLimit(int rateLimitKBPS);
	
	/**
	 * Get the current rate limit for loading from a single host.  The {@link Browser} will wait until the rate is below
	 * this threshold before making another request.  If it is <code>0</code>, rate limiting is disabled.
	 * @return The current {@link int} rate limit in kilobytes per second, or <code>0</code> if it is disabled.
	 * @see #setRateLimit(int)
	 */
	public abstract int getRateLimit();
	
	/**
	 * @param timeout How many seconds before giving up on a request.
	 */
	public abstract void setTimeout(int timeout);
	
	/**
	 * @param maxResponseSizeKB The maximum size of a response in kilobytes that this {@link Browser}
	 * will load before terminating.  Since responses are fed straight through to a regex
	 * parser, it is wise not to deal with huge pages.
	 */
	public abstract void setMaxResponseSize(int maxResponseSizeKB);
}
