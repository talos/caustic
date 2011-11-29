package net.caustic.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.RateLimitManager;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.regexp.Pattern;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StringUtils;

/**
 * The {@link HttpBrowser} can be used by to make
 * HTTP requests and handle the responses.
 */
public class HttpBrowser implements Loggable {
	public static final String COOKIE_HEADER_NAME = "Cookie";
	public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";

	/**
	 * The default number of seconds to wait before timing out on a
	 * request for {@link HttpBrowser} interfaces.
	 */
	public static final int DEFAULT_TIMEOUT_MILLISECONDS = 30000;

	/**
	 * The default rate limit a {@link RateLimitManager} interface imposes upon itself for
	 * a single host.
	 */
	public static final int DEFAULT_RATE_LIMIT = 30;
	
	/**
	 * How many milliseconds to wait before placing a second request ot
	 * a single host.
	 */
	public static final int DEFAULT_REQUEST_WAIT = 1000;
	
	/**
	 * The default number of redirects {@link HttpBrowser} interfaces will follow.
	 */
	public static final int MAX_REDIRECTS = 50;
	
	/**
	 * How many kilobytes of a response a {@link HttpBrowser} should allow before 
	 * automatically terminating the request.
	 */
	public static final int DEFAULT_MAX_RESPONSE_SIZE = 2 * 1024;
	
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public static final String USER_AGENT_HEADER_NAME = "User-Agent";
	public static final String USER_AGENT_HEADER_DEFAULT_VALUE = "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)";
	
	public static final String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";
	public static final String ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE = "en-US,en;q=0.8";
	
	public static final String ACCEPT_HEADER_NAME = "Accept";
	public static final String ACCEPT_HEADER_DEFAULT_VALUE = "*/*";
	//public static final String ACCEPT_HEADER_JSON_VALUE = "application/json,text/json";
	
	public static final String GET = "get";
	public static final String POST = "post";
	public static final String HEAD = "head";
	
	private final RateLimitManager rateLimitManager;
	private final HttpRequester requester;
	private final HttpUtils utils;
	private final Encoder encoder;
	//private final CookieManager cookieManager;
	
	private int maxResponseSize = HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE;
	private final MultiLog log;
	
	/**
	 * How many milliseconds this {@link HttpBrowser} will sleep before
	 * considering trying a host again, when its {@link RateLimitManager} tells
	 * it that a request should be held off.
	 */
	public static final int DEFAULT_SLEEP_TIME = 500;

	/**
	 * Add generic headers.
	 * @param referer The {@link String} URL to use as the referer.
	 * @return A {@link Hashtable} of generic headers.
	 */
	private Hashtable getGenericHeaders(String referer) {
		Hashtable headers = new Hashtable();
		// Add generic headers.
		headers.put(ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE);
		headers.put(ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE);
		headers.put(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE);
		headers.put(REFERER_HEADER_NAME, referer); // default to the current URL as referer.
		
		return headers;
	}
	
	/**
	 * Make an HTTP request, following redirects.
	 * @param method The HTTP method to use.
	 * @param urlStr The {@link String} URL to request.
	 * @param headers A {@link Hashtable} of additional headers, which are {@link String} to
	 * {@link String} name-value pairs.
	 * @param encodedPostData {@link String} of post data, already encoded. <code>Null</code> if
	 * none.
	 * @param db A {@link Database} to persist cookies to.
	 * @param scope A {@link Scope} specifying where in <code>db</code> to persist cookies.
	 * @return A {@link InputStreamReader} to read response content, if it was a request that
	 * should return content.
	 * @throws InterruptedException If the user interrupted the request while it was being delayed
	 * due to rate limiting, or while waiting for the host to respond.
	 * @throws HttpRequestException If the request could not be completed.
	 * @throws CookieStorageException If a cookie could not be stored from one of the responses.
	 * @throws DatabaseException if there was an error persisting cookies.
	 */
	private InputStreamReader request(String method, String urlStr, Hashtable headers,
					String encodedPostData, Database db, Scope scope)
			throws InterruptedException, HttpRequestException, DatabaseException {
		
		final Vector urlsRead = new Vector();
		
		while(urlsRead.size() <= MAX_REDIRECTS) {
			
			
			while(rateLimitManager.shouldDelay(urlStr) == true) {
				Thread.sleep(DEFAULT_SLEEP_TIME);
				if(Thread.interrupted()) {
					throw new InterruptedException("Interrupted while waiting to not exceed rate limit.");
				}
			}
			
			// Merge in generic headers.
			headers = HashtableUtils.combine(new Hashtable[] { getGenericHeaders(urlStr), headers });
			
			// Add cookies into the headers.
			String[] cookies = db.getCookies(scope, urlStr, encoder);
			//String[] cookies = cookieManager.getCookiesFor(urlStr, headers);
			//String[] cookie2s = cookieManager.getCookie2sFor(urlStr, headers);
			
			if(cookies.length > 0) {
				headers.put(COOKIE_HEADER_NAME, StringUtils.join(cookies, "; "));
			}
			/*
			if(cookie2s.length > 0) {
				headers.put(CookieManager.COOKIE_2_HEADER_NAME, StringUtils.join(cookie2s, "; "));
			}*/
			
			log.i("All headers: " + StringUtils.quote(headers));
			
			log.i("Requesting " + method + " from " + StringUtils.quote(urlStr));
					
			HttpResponse response;
			if(method.equals(HEAD)) {
				response = requester.head(urlStr, headers);
			} else if(method.equals(GET)) {
				response = requester.get(urlStr, headers);
			} else {
				response = requester.post(urlStr, headers, encodedPostData);
				log.i("Post data: " + StringUtils.quote(encodedPostData));
			}
			
			// Add cookies from the response to the database.
			String[] responseCookies = response.getResponseHeaders().getHeaderValues(SET_COOKIE_HEADER_NAME);
			if(responseCookies != null) {
				for(int i = 0 ; i < responseCookies.length ; i ++) {
					String[] nameValue = StringUtils.split(StringUtils.split(responseCookies[i], "; ")[0], "=");
					db.addCookie(scope, utils.getHost(urlStr), nameValue[0], nameValue[1]);
				}
			}
			
			if(response.isSuccess()) {
				// Only return the content stream for non-head requests.
				if(method.equals(HEAD)) {
					return null;
				} else {
					return response.getContentStream();
				}
			} else if(!response.isRedirect()) {
				throw new BadHttpResponseCode(response.getResponseCode());
			} else {
				try {
					String redirectURLStr = response.getRedirectLocation();
					
					if(urlsRead.contains(urlStr)) {
						String[] redirectsFollowedAry = new String[urlsRead.size()];
						urlsRead.copyInto(redirectsFollowedAry);
						throw HttpRedirectException.newCircular(redirectsFollowedAry);
					} else {
						urlsRead.add(urlStr);
					}
					
					log.i("Following redirect #" + Integer.toString(urlsRead.size()) +
							" from " + StringUtils.quote(urlStr) + " to " + StringUtils.quote(redirectURLStr));
					
					// loops back to top
					method = GET;
					urlStr = redirectURLStr;
					//return request(GET, redirectURLStr, headers, null, urlsRead, db, scope);
				} catch(BadURLException e) {
					throw HttpRedirectException.fromBadURL(e);
				}
			}
		}
		
		// hit max # of redirects.
		String[] redirectsFollowedAry = new String[urlsRead.size()];
		urlsRead.copyInto(redirectsFollowedAry);
		throw HttpRedirectException.newMaxRedirects(redirectsFollowedAry, urlsRead.size());
	}
	
	/**
	 * Pull an {@link InputStreamReader} into a {@link String}, allowing for early termination.
	 * @param urlStr The {@link String} URL from which the {@link InputStreamReader} is a response.
	 * @param stream An {@link InputStreamReader} response from <code>url</code>
	 * @param terminates array of {@link Pattern}s to interrupt the load.
	 * @return A {@link String}.
	 * @throws InterruptedException if the user interrupted the load.
	 * @throws HttpResponseContentException if the response could not be fully read.
	 */
	private String readResponseStream(String urlStr, InputStreamReader stream, Pattern[] terminates)
			throws InterruptedException, HttpResponseContentException {
		
		StringBuffer responseBody = new StringBuffer();
		
		char[] buffer = new char[512];
		int totalReadBytes = 0;
		int lastTotalReadBytes = totalReadBytes;
		int readBytes;
		boolean terminate = false;
		
		try { // try/catch for reading the stream
			while((readBytes = stream.read(buffer)) != -1 && terminate == false) {

				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				totalReadBytes += readBytes;
				// log every 51.2 kB
				if(totalReadBytes - lastTotalReadBytes > buffer.length * 100) { 
					log.i("Have loaded " + totalReadBytes + " bytes from " + StringUtils.quote(urlStr));
					lastTotalReadBytes = totalReadBytes;
				}
				
				responseBody.append(buffer, 0, readBytes);
				
				if(totalReadBytes > maxResponseSize * 1024) {
					throw new IOException("Exceeded maximum response size of " + maxResponseSize + "KB.");
				}
				
				for(int i = 0 ; i < terminates.length && terminate == false ; i++) {
					if(terminates[i].matches(responseBody.toString(), Pattern.FIRST_MATCH)) {
						log.i("Terminating " + urlStr.toString() + " due to pattern " + terminates[i].toString());
						terminate = true;
					}
				}
			}
		} catch (IOException e) {
			throw HttpResponseContentException.fromIOException(e);
		}
		
		try { // try/catch for closing the stream.
			stream.close();
		} catch (IOException e) {
			throw HttpResponseContentException.fromIOException(e);
		}
		rateLimitManager.rememberResponse(urlStr, responseBody.length());
		return responseBody.toString();
	}
	
	public HttpBrowser(HttpRequester requester, RateLimitManager rateLimitManager,
			HttpUtils utils, Encoder encoder) {
		this.requester = requester;
		this.rateLimitManager = rateLimitManager;
		this.utils = utils;
		this.encoder = encoder;
		this.log = new MultiLog();
	}
	
	/**
	 * 
	 * @return A copy of this {@link HttpBrowser} with a copied {@link CookieManager}.
	 * This cookie manager will have a copy of old cookies in it, but new cookies
	 * will not affect other scrapers.  Has the same loggers.
	 */
	/*public HttpBrowser copy() {
		return new HttpBrowser(requester, rateLimitManager, cookieManager.copy(), log);
	}*/
	
	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from response headers to the {@link HttpBrowser}'s cookie store.
	 * @param urlStr the URL to HTTP Head.
	 * @param headers {@link Hashtable} extra headers.
	 * @param db A {@link Database} to persist cookies to.
	 * @param scope A {@link Scope} specifying where in <code>db</code> to persist cookies.
	 * @throws InterruptedException If the user interrupted the request.
	 * @throws HttpException if there was an exception that prevented the request from being completed.
	 * @throws DatabaseException if there was an error persisting cookies.
	 */
	public void head(String urlStr, Hashtable headers, Database db, Scope scope)
			throws InterruptedException, HttpException, DatabaseException {
		request(HEAD, urlStr, headers, null, db, scope);
	}
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param urlStr the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @param db A {@link Database} to persist cookies to.
	 * @param scope A {@link Scope} specifying where in <code>db</code> to persist cookies.
	 * @return The body of the response.
	 * @throws InterruptedException If the user interrupted the request.
	 * @throws HttpException if there was an exception that prevented the request from being completed or
	 * its response from being read.
	 * @throws DatabaseException if there was an error persisting cookies.
	 */
	public String get(String urlStr, Hashtable headers, Pattern[] terminates, Database db, Scope scope)
				throws InterruptedException, HttpException, DatabaseException {
		InputStreamReader stream = request(GET, urlStr, headers, null, db, scope);
		return readResponseStream(urlStr, stream, terminates);
	}
	
	/**
	 * Make an HTTP Post request with a {@link String} to encode into post data.
	 * This returns the body of the response, and adds cookies to the cookie jar.
	 * @param urlStr the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @param encodedPostData {@link String} of post data.  Should already be encoded.
	 * @param db A {@link Database} to persist cookies to.
	 * @param scope A {@link Scope} specifying where in <code>db</code> to persist cookies.
	 * @return The body of the response.
	 * @throws InterruptedException If the user interrupted the request.
	 * @throws HttpException if there was an exception that prevented the request from being completed or
	 * its response from being read.
	 * @throws DatabaseException if there was an error persisting cookies.
	 */
	public String post(String urlStr, Hashtable headers, Pattern[] terminates, String encodedPostData,
			Database db, Scope scope)
				throws InterruptedException, HttpException, DatabaseException {
		InputStreamReader stream = request(POST, urlStr, headers, encodedPostData, db, scope);
		return readResponseStream(urlStr, stream, terminates);
	}
	
	/**
	 * @param maxResponseSizeKB The maximum size of a response in kilobytes that this {@link HttpBrowser}
	 * will load before terminating.  Since responses are fed straight through to a regex
	 * parser, it is wise not to deal with huge pages.
	 */
	public void setMaxResponseSize(int maxResponseSizeKB) {
		this.maxResponseSize = maxResponseSizeKB;
	}

	public void register(Logger logger) {
		log.register(logger);
	}
	
	/**
	 * Add cookies to the {@link Browser}'s {@link CookieManager}, suppressing the 
	 * {@link BadURLException} that can occur.
	 * @param urlStr The {@link String} URL to use for the domain and path of the added cookies.
	 * @param cookies A {@link Hashtable} mapping {@link String} to {@link String} to use
	 * as name-value pairs for the cookies. 
	 */
	/*public void addCookies(String urlStr, Hashtable cookies) {
		try {
			cookieManager.addCookies(urlStr, cookies);
		} catch(BadURLException e) {
			log.i("Could not add cookies: " + e.getMessage());
		}
	}*/
	/**
	 * 
	 * @param timeoutMilliseconds How many milliseconds to wait for a response from the remote server before giving up.
	 */
	public void setTimeout(int timeoutMilliseconds) {
		requester.setTimeout(timeoutMilliseconds);
	}

	/**
	 * Change this {@link Browser}'s enforced rate limit.
	 * @param rateLimitKBps The rate limit to use, in kilobytes per second.
	 */
	public void setRateLimit(int rateLimitKBps) {
		rateLimitManager.setRateLimit(rateLimitKBps);
	}

	/**
	 * Change this {@link Browser}'s minimum wait before repeated requests to a single host.
	 * @param minRequestWaitMilliseconds The number of milliseconds to wait between requests to a single host, in milliseconds.
	 */
	public void setMinRequestWait(int minRequestWaitMilliseconds) {
		rateLimitManager.setMinRequestWait(minRequestWaitMilliseconds);
	}
}
