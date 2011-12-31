package net.caustic.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.http.RateLimitManager;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.regexp.Pattern;
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
	//private final CookieManager cookieManager;
	
	private int maxResponseSize = HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE;
	private final MultiLog log;
	private final HttpUtils utils;
	
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
	 */
	public BrowserResponse request(String url, String method, Hashtable headers, Cookies cookies,
				String encodedPostData)
			throws InterruptedException, HttpException {
		
		final Vector urlsRead = new Vector();
		HashtableCookies responseCookies = new HashtableCookies();
		
		while(urlsRead.size() <= MAX_REDIRECTS) {
			
			while(rateLimitManager.shouldDelay(url) == true) {
				Thread.sleep(DEFAULT_SLEEP_TIME);
				if(Thread.interrupted()) {
					throw new InterruptedException("Interrupted while waiting to not exceed rate limit.");
				}
			}
			
			// Merge in generic headers.
			headers = HashtableUtils.combine(new Hashtable[] { getGenericHeaders(url), headers });
			
			String[] cookiesForURL = cookies.get(utils.getHost(url));
			if(cookiesForURL != null) {
				headers.put(COOKIE_HEADER_NAME, StringUtils.join(cookiesForURL, "; "));
			}

			log.i("All headers: " + StringUtils.quote(headers));
			
			log.i("Requesting " + method + " from " + StringUtils.quote(url));
			
			HttpResponse response;
			if(method.equals(HEAD)) {
				response = requester.head(url, headers);
			} else if(method.equals(GET)) {
				response = requester.get(url, headers);
			} else {
				response = requester.post(url, headers, encodedPostData);
				log.i("Post data: " + StringUtils.quote(encodedPostData));
			}
			
			// Add cookies from the response to the database.
			String[] cookieHeaders = response.getResponseHeaders().getHeaderValues(SET_COOKIE_HEADER_NAME);
			if(cookieHeaders != null) {
				for(int i = 0 ; i < cookieHeaders.length ; i ++) {
					responseCookies.add(utils.getHost(url), cookieHeaders[i]);
				}
			}
			
			if(response.isSuccess()) {
				final String content;
				// Only return the content stream for non-head requests.
				if(method.equals(HEAD)) {
					content = null;
				} else {
					content = readResponseStream(url, response.getContentStream(), new Pattern[0]);
				}
				// TODO this is spaghetti.
				return new BrowserResponse(content, responseCookies);
			} else if(!response.isRedirect()) {
				throw new BadHttpResponseCode(response.getResponseCode());
			} else {
				try {
					String redirectURLStr = response.getRedirectLocation();
					
					if(urlsRead.contains(url)) {
						String[] redirectsFollowedAry = new String[urlsRead.size()];
						urlsRead.copyInto(redirectsFollowedAry);
						throw HttpRedirectException.newCircular(redirectsFollowedAry);
					} else {
						urlsRead.add(url);
					}
					
					log.i("Following redirect #" + Integer.toString(urlsRead.size()) +
							" from " + StringUtils.quote(url) + " to " + StringUtils.quote(redirectURLStr));
					
					// loops back to top
					method = GET;
					url = redirectURLStr;
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
	 * @param terminates  of {@link Pattern}s to interrupt the load.
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
	
	public HttpBrowser(HttpRequester requester, RateLimitManager rateLimitManager, HttpUtils utils) {
		this.requester = requester;
		this.rateLimitManager = rateLimitManager;
		this.log = new MultiLog();
		this.utils = utils;
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
