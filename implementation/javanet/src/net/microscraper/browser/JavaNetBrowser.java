package net.microscraper.browser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Cookie;
import net.microscraper.client.Logger;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.StringUtils;

/**
 * A browser implementation using {@link java.net.HttpURLConnection} and 
 * {@link java.net.CookieManager}.
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	private final CookieManager cookieManager = new CookieManager();
	private final RateLimitMemory rateLimitMemory = new RateLimitMemory();
	
	private int rateLimitKBPS = Browser.DEFAULT_RATE_LIMIT;
	private int timeout = Browser.DEFAULT_TIMEOUT;
	private int maxResponseSize = Browser.DEFAULT_MAX_RESPONSE_SIZE;
	private final BasicLog log = new BasicLog();
	
	/**
	 * Request a {@link HttpURLConnection}, and follow any redirects while adding cookies.
	 * @param method The {@link Method} to use.
	 * @param urlStr A URL to load.  Also defaults to be the Referer in the request header.
	 * @param postData A {@link String} of post data to send.
	 * @param headers A {@link Hashtable} of additional headers.
	 * @return A {@link HttpURLConnection} upon which {@link HttpURLConnection#connect()} has already been called.
	 * @throws IOException If there was an error generating the {@link HttpURLConnection}.
	 */
	private HttpURLConnection request(String method, String urlStr, String postData,
					Hashtable<String, String> headers)
				throws IOException {
		return request(method, urlStr, postData, headers, new ArrayList<String>());
	}

	/**
	 * Request a {@link HttpURLConnection}, and follow any redirects while adding cookies.
	 * @param method The {@link Method} to use.
	 * @param urlStr A URL to load.  Also defaults to be the Referer in the request header.
	 * @param postData A {@link String} of post data to send.
	 * @param headers A {@link Hashtable} of additional headers.
	 * @param redirectsFollowed A {@link List} of redirect {@link String}s already followed.
	 * @return A {@link HttpURLConnection} upon which {@link HttpURLConnection#connect()} has already been called.
	 * @throws IOException If there was an error generating the {@link HttpURLConnection}.
	 */
	private HttpURLConnection request(String method, String urlStr, String postData,
					Hashtable<String, String> headers, List<String> redirectsFollowed)
				throws IOException {		
		HttpURLConnection.setFollowRedirects(false); // this is handled manually
		HttpURLConnection conn = (HttpURLConnection) (new URL(urlStr)).openConnection();	
		
		// Add generic headers.
		conn.setRequestProperty(ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE);
		conn.setRequestProperty(ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE);
		conn.setRequestProperty(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE);
		conn.setRequestProperty(REFERER_HEADER_NAME, urlStr); // default to the current URL as referer.
		
		// Add cookies.
		try {
			Map<String, List<String>> cookieHeaders = cookieManager.get(conn.getURL().toURI(), conn.getRequestProperties());
			Iterator<String> iterator = cookieHeaders.keySet().iterator();
			while(iterator.hasNext()) {
				String headerName = iterator.next();
				List<String> cookies = cookieHeaders.get(headerName);
				conn.setRequestProperty(headerName, StringUtils.join(cookies.toArray(new String[cookies.size()]), "; "));
			}
		} catch(URISyntaxException e) {
			log.i("Could not use cookies for " + urlStr + " because it is not a valid URI: " + e.getMessage());
		}
		
		// Add additional headers
		Enumeration<String> headerNames = headers.keys();
		while(headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			String headerValue = (String) headers.get(headerName);
			conn.setRequestProperty(headerName, headerValue);
		}
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(timeout * 1000);
		
		// Set method
		if(method.equalsIgnoreCase(Browser.POST)) {
			conn.setRequestMethod("POST");
			OutputStreamWriter writer = null;
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(postData);
			writer.flush();
		} else {
			conn.setRequestMethod(method.toUpperCase());
		}
		
		// Try to connect.
		try {
			conn.connect();
		} catch(SocketTimeoutException e) {
			throw new IOException("Timeout after " + conn.getReadTimeout() + " seconds " +
					", " + e.bytesTransferred + " bytes transferred.");
		}

		// Try to add cookies from response headers to cookie manager.
		try {
			cookieManager.put(conn.getURL().toURI(), conn.getHeaderFields());
		} catch(URISyntaxException e) {
			log.i("Could not save cookie because URL could not be converted to URI: " + e.getMessage());
		}
		
		// Handle response code, returning the connection if it's OK, trying to follow redirects otherwise.
		int code = conn.getResponseCode();		
		HttpURLConnection result;	
		if(code == HttpURLConnection.HTTP_OK) {
			result = conn;
		} else if(code < 300 || code >= 400) { // code that can't be handled
			throw new IOException("Aborting due to response code " + StringUtils.quote(code) + ".");
		} else { // redirect code.
			if(redirectsFollowed.size() >= MAX_REDIRECTS) {
				throw new IOException("Max redirects exhausted.");
			}
			
			String redirectString = conn.getHeaderField(LOCATION_HEADER_NAME);
			if(redirectsFollowed.contains(redirectString)) {
				throw new IOException("Not following circular redirect from " +
						conn.getURL().toString() + " to " + redirectString);
			} else {		
				redirectsFollowed.add(redirectString);
			}
			
			log.i("Following redirect #" + Integer.toString(redirectsFollowed.size()) +
					" from " + StringUtils.quote(conn.getURL()) + " to " + StringUtils.quote(redirectString));
			
			String resolvedUrlString;
			try {
				resolvedUrlString = conn.getURL().toURI().resolve(redirectString).toString();
			} catch(URISyntaxException e) {
				throw new IOException("Unable to resolve redirect from " + StringUtils.quote(conn.getURL())
						+ " to " + redirectString + ": " + e.getMessage());
			}
			
			result = request(Browser.GET, resolvedUrlString, null, headers, redirectsFollowed);
		}
		return result;
	}
	

	/**
	 * Check {@link #rateLimitMemory} to see whether a request to <code>urlStr</code> should be delayed,
	 * and hold the thread accordingly.
	 * @param urlStr The {@link String} URL to check.
	 * @throws InterruptedException If the user interrupts during a delay
	 */
	private void obeyRateLimit(String urlStr) throws InterruptedException {
		if(rateLimitKBPS > 0) {
			float kbpsSinceLastLoad = rateLimitMemory.getRateSinceLastResponseFrom(urlStr);
			log.i("Load speed from " + urlStr + " : " + Float.toString(kbpsSinceLastLoad));
			if(kbpsSinceLastLoad > rateLimitKBPS) {
				log.i("Delaying load of " + StringUtils.quote(urlStr) + ", current KBPS " +
							StringUtils.quote(Float.toString(kbpsSinceLastLoad)));
				Thread.sleep(Browser.DEFAULT_SLEEP_TIME);
			}
		}
	}
	
	/**
	 * Pull an {@link InputStream} into a {@link String}, allowing for premature termination.
	 * @param url The {@link String} URL from which the {@link InputStream} is a response.
	 * @param stream A {@link InputStream} response from <code>url</code>
	 * @param terminates array of {@link Pattern}s to interrupt the load.
	 * @return A {@link String}.
	 * @throws IOException if there was an exception requesting.
	 * @throws InterruptedException if the user interrupted the load.
	 */
	private String pullResponseFromStream(String url, InputStream stream, Pattern[] terminates)
			throws IOException, InterruptedException {
		String responseBody;
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[512];
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
				log.i("Have loaded " + totalReadBytes + " bytes from " + StringUtils.quote(url));
				lastTotalReadBytes = totalReadBytes;
			}
			content.write(buffer, 0, readBytes);
			responseBody = new String(content.toByteArray());
			if(totalReadBytes > maxResponseSize * 1024) {
				throw new IOException("Exceeded maximum response size of " + maxResponseSize + "KB.");
			}
			
			for(int i = 0 ; i < terminates.length && terminate == false ; i++) {
				if(terminates[i].matches(responseBody, Pattern.FIRST_MATCH)) {
					log.i("Terminating " + url.toString() + " due to pattern " + terminates[i].toString());
					terminate = true;
				}
			}
		}
		stream.close();

		responseBody = content.toString();
		rateLimitMemory.rememberResponse(url, responseBody.length());
		return responseBody;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void head(String url, Hashtable headers) throws IOException, InterruptedException {
		request(Browser.HEAD, url, null, headers);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String get(String url, Hashtable headers, Pattern[] terminates) throws
			IOException, InterruptedException {
		HttpURLConnection conn = request(Browser.GET, url, null, headers, new Vector<String>());
		obeyRateLimit(url);
		return pullResponseFromStream(url, conn.getInputStream(), terminates);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String post(String url, Hashtable headers, Pattern[] terminates, String postData)
				throws IOException, InterruptedException {
		HttpURLConnection conn = request(Browser.POST, url, postData, headers, new Vector<String>());
		obeyRateLimit(url);
		return pullResponseFromStream(url, conn.getInputStream(), terminates);
	}
	
	public void setRateLimit(int rateLimitKBPS) {
		this.rateLimitKBPS = rateLimitKBPS;
	}

	public int getRateLimit() {
		return rateLimitKBPS;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setMaxResponseSize(int maxResponseSizeKB) {
		this.maxResponseSize = maxResponseSizeKB;
	}

	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void addCookies(Cookie[] cookies) {
		CookieStore cookieStore = cookieManager.getCookieStore();
		for(int i = 0 ; i < cookies.length ; i ++) {
			try {
				cookieStore.add(new URI(cookies[i].getUrl()), new HttpCookie(cookies[i].getName(), cookies[i].getValue()));
			} catch(URISyntaxException e) {
				log.i("Could not add cookie with URL " + cookies[i].getUrl() + ": " + e.getMessage());
			}
		}
	}
}
