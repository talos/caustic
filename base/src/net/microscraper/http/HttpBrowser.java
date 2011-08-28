package net.microscraper.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.http.RateLimitManager;
import net.microscraper.log.BasicLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.StringUtils;

/**
 * The {@link HttpBrowser} can be used by to make
 * HTTP requests and handle the responses.
 */
public class HttpBrowser implements Loggable {
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
	
	public static final String UTF_8 = "UTF-8";

	public static final String GET = "get";
	public static final String POST = "post";
	public static final String HEAD = "head";
	
	private final RateLimitManager rateLimitManager;
	private final HttpRequester requester;
	private final CookieManager cookieManager;
	
	private int maxResponseSize = HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE;
	private final BasicLog log = new BasicLog();

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
	 * @return A {@link InputStreamReader} to read response content, if it was a request that
	 * should return content.
	 * @throws IOException If there was an error generating the {@link HttpURLConnection}.
	 */
	private InputStreamReader request(String method, String urlStr, Hashtable headers, String encodedPostData)
			throws InterruptedException, IOException {
		return request(method, urlStr, headers, encodedPostData, new Vector());
	}

	/**
	 * Make an HTTP request, following redirects.
	 * @param method The HTTP method to use.
	 * @param urlStr The {@link String} URL to request.
	 * @param headers A {@link Hashtable} of additional headers, which are {@link String} to
	 * {@link String} name-value pairs.
	 * @param encodedPostData {@link String} of post data, already encoded. <code>Null</code> if
	 * none.
	 * @return A {@link InputStreamReader} to read response content, if it was a request that
	 * should return content.
	 * @throws IOException If there was an error generating the {@link HttpURLConnection}.
	 */
	private InputStreamReader request(String method, String urlStr, Hashtable headers, String postData, Vector redirectsFollowed)
			throws InterruptedException, IOException {
		
		// Merge in generic headers.
		headers = HashtableUtils.combine(new Hashtable[] { getGenericHeaders(urlStr), headers });
		
		// Add cookies into the headers.
		try {
			String[] cookies = cookieManager.getCookiesFor(urlStr, headers);
			String[] cookie2s = cookieManager.getCookie2sFor(urlStr, headers);

			headers.put(CookieManager.COOKIE_HEADER_NAME, StringUtils.join(cookies, "; "));
			headers.put(CookieManager.COOKIE_2_HEADER_NAME, StringUtils.join(cookie2s, "; "));
		} catch(BadURLException e) {
			throw new IOException(e);
		}
		
		HttpResponse response;
		if(method.equals(HEAD)) {
			response = requester.head(urlStr, headers);
		} else if(method.equals(GET)) {
			response = requester.get(urlStr, headers);
		} else {
			response = requester.post(urlStr, headers, postData);
		}
		
		// Add cookies from the response.
		try {
			cookieManager.addCookiesFromResponseHeaders(urlStr, response.getResponseHeaders());
		} catch(BadURLException e) {
			log.i("Could not add cookie because of bad URL: " + e.getMessage());
		}
		
		if(response.isSuccess()) {
			// Only return the content stream for non-head requests.
			if(method.equals(HEAD)) {
				return null;
			} else {
				return response.getContentStream();
			}
		} else if(!response.isRedirect()) {
			throw new IOException("Bad response code from " + StringUtils.quote(urlStr) +
					": " + response.getResponseCode());
		} else {
			if(redirectsFollowed.size() >= MAX_REDIRECTS) {
				throw new IOException("Max redirects exhausted.");
			}
			try {
				String redirectURLStr = response.getRedirectLocation();
				if(redirectsFollowed.contains(redirectURLStr)) {
					throw new IOException("Not following circular redirect from " +
							StringUtils.quote(urlStr) + " to " + StringUtils.quote(redirectURLStr));
				} else {		
					redirectsFollowed.add(redirectURLStr);
				}

				log.i("Following redirect #" + Integer.toString(redirectsFollowed.size()) +
						" from " + StringUtils.quote(urlStr) + " to " + StringUtils.quote(redirectURLStr));
				
				return request(GET, redirectURLStr, headers, null, redirectsFollowed);
			} catch(BadURLException e) {
				throw new IOException("Could not follow redirect: " + e.getMessage());
			}
		}	
	}
	
	/**
	 * Pull an {@link InputStream} into a {@link String}, allowing for early termination.
	 * @param urlStr The {@link String} URL from which the {@link InputStream} is a response.
	 * @param stream An {@link InputStreamReader} response from <code>url</code>
	 * @param terminates array of {@link Pattern}s to interrupt the load.
	 * @return A {@link String}.
	 * @throws IOException if there was an exception requesting.
	 * @throws InterruptedException if the user interrupted the load.
	 */
	private String readResponseStream(String urlStr, InputStreamReader stream, Pattern[] terminates)
			throws IOException, InterruptedException {
		rateLimitManager.obeyRateLimit(urlStr, log);
		
		StringBuffer responseBody = new StringBuffer();
		
		char[] buffer = new char[512];
		int totalReadBytes = 0;
		int lastTotalReadBytes = totalReadBytes;
		int readBytes;
		boolean terminate = false;
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
		stream.close();
		
		rateLimitManager.rememberResponse(urlStr, responseBody.length());
		return responseBody.toString();
	}
	
	public HttpBrowser(HttpRequester requester, RateLimitManager rateLimitManager, CookieManager cookieManager) {
		this.requester = requester;
		this.rateLimitManager = rateLimitManager;
		this.cookieManager = cookieManager;
	}
	
	/**
	 * Make an HTTP Head request.  This does not return anything, but it should add any cookies
	 * from response headers to the {@link HttpBrowser}'s cookie store.
	 * @param urlStr the URL to HTTP Head.
	 * @param headers {@link Hashtable} extra headers.
	 * @throws IOException If there was an exception requesting the page.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public void head(String urlStr, Hashtable headers) throws IOException, InterruptedException {
		request(HEAD, urlStr, headers, null);
	}
	
	/**
	 * Make an HTTP Get request.  This returns the body of the response, and adds cookies to the cookie jar.
	 * @param urlStr the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @return The body of the response.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public String get(String urlStr, Hashtable headers, Pattern[] terminates)
				throws IOException, InterruptedException {
		InputStreamReader stream = request(GET, urlStr, headers, null);
		return readResponseStream(urlStr, stream, terminates);
	}
	
	/**
	 * Make an HTTP Post request with a {@link String} to encode into post data.
	 * This returns the body of the response, and adds cookies to the cookie jar.
	 * @param urlStr the URL to HTTP Get.
	 * @param headers {@link Hashtable} extra headers.
	 * @param terminates Array of {@link Pattern}s that prematurely terminate the load and return the body.
	 * @param encodedPostData {@link String} of post data.  Should already be encoded.
	 * @return The body of the response.
	 * @throws IOException If there was an exception making or during the request.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	public String post(String urlStr, Hashtable headers, Pattern[] terminates, String encodedPostData)
				throws IOException, InterruptedException {
		InputStreamReader stream = request(POST, urlStr, headers, encodedPostData);
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
	public void addCookies(String urlStr, Hashtable cookies) {
		try {
			cookieManager.addCookies(urlStr, cookies);
		} catch(BadURLException e) {
			log.i("Could not add cookies: " + e.getMessage());
		}
	}
}
