package net.microscraper.browser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Cookie;
import net.microscraper.client.Loggable;
import net.microscraper.client.Logger;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.Encoder;
import net.microscraper.util.StringUtils;

/**
 * This is a very lightweight {@link Browser}.  It will add headers & posts, and does <b>very</b>
 * primitive cookie handling.  It only looks at the name & value of cookies -- not expiration or domains.
 * @author john
 *
 */
public class JavaNetBrowser implements Browser, Loggable {
	private static final String encoding = UTF_8;
	private final Hashtable cookieStore = new Hashtable();
	private final HostMemory hostMemory = new HostMemory();
	private int rateLimitKBPS = Browser.DEFAULT_RATE_LIMIT;
	private int timeout = Browser.DEFAULT_TIMEOUT;
	private int maxResponseSize = Browser.DEFAULT_MAX_RESPONSE_SIZE;
	private final BasicLog log = new BasicLog();
	private final Encoder encoder = new JavaNetEncoder();
	
	public void head(String url, Hashtable headers)
			throws IOException, InterruptedException {
		log.i("Retrieving Head from  " + StringUtils.quote(url.toString()) + "...");
		connectHandlingRedirectCookies("HEAD", new URL(url.toString()), null, headers);
	}

	public String get(String url, Hashtable headers,
			Pattern[] terminates) throws
			IOException, InterruptedException {
		log.i("Getting  " + url.toString() + "...");
		InputStream stream = connectHandlingRedirectCookies("GET", new URL(url.toString()), null, headers);
		return pullResponse(url, stream, terminates);
	}

	public String post(String url, Hashtable headers, Pattern[] terminates, Hashtable posts)
				throws IOException, InterruptedException {
		log.i("Posting to  " + url.toString() + "...");

		String postData = "";
		Enumeration keys = posts.keys();
		while(keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			String value = (String) posts.get(name);
			postData += encoder.encode(name, encoding) + '=' + encoder.encode(value, encoding) + '&';
		}
		postData = postData.substring(0, postData.length() -1); // trim trailing ampersand
		
		InputStream stream = connectHandlingRedirectCookies("POST", new URL(url.toString()), postData, headers);
		return pullResponse(url, stream, terminates);
	}

	public String post(String url, Hashtable headers, Pattern[] terminates,
			String postData)
				throws IOException, InterruptedException {
		log.i("Posting to  " + url.toString() + "...");
		InputStream stream = connectHandlingRedirectCookies("POST", new URL(url.toString()), postData, headers);
		return pullResponse(url, stream, terminates);
	}
	
	/**
	 * Get the body of the response using an already-primed HttpURLConnection.  Can be terminated prematurely
	 * by a pattern.
	 * @param conn a connection ready for {@link java.net.URLConnection#getInputStream}.
	 * @param terminates array of {@link Pattern}s to interrupt the load. Can be <code>null</code>.
	 * @return the response body.
	 * @throws IOException if there was an exception requesting.
	 * @throws InterruptedException if the user interrupted the load.
	 */
	private String pullResponse(String url, InputStream stream, Pattern[] terminates)
			throws IOException, InterruptedException {

		//URL url = conn.getURL();
		String responseBody;
		ByteArrayOutputStream content = new ByteArrayOutputStream();

		// Pull response.
		//InputStream stream = conn.getInputStream();
		byte[] buffer = new byte[512];
		int totalReadBytes = 0;
		int lastTotalReadBytes = totalReadBytes;
		int readBytes;
		loading: while((readBytes = stream.read(buffer)) != -1) {
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
			if(terminates != null && terminates.length > 0) {
				for(int i = 0 ; i < terminates.length ; i++) {
					if(terminates[i].matches(responseBody, Pattern.FIRST_MATCH)){
						log.i("Terminating " + url.toString() + " due to pattern " + terminates[i].toString());
						break loading;
					}
				}
			}
		}
		stream.close();

		responseBody = content.toString();
		try {
			hostMemory.add(new URL(url.toString()), responseBody.length());
		} catch (MalformedURLException e) {
			hostMemory.add(url.toString(), responseBody.length());
		}
		log.i("Response body: " + responseBody);
		return responseBody;
	}
	
	private void addHeaderToConnection(HttpURLConnection conn, String name, String value) {
		log.i("Adding header " + StringUtils.quote(name) + " : " + StringUtils.quote(value));
		conn.setRequestProperty(name, value);
	}
	
	/**
	 * Generate an {@link java.net.HttpURLConnection}.  Defaults the Referer header to the current url.
	 * @param method The {@link Method} to use.
	 * @param url A {@link URL} to load.  Also defaults to be the Referer in the request header.
	 * @param postData A {@link String} of POST data to send.
	 * @param headers A {@link Hashtable} of special headers to use.
	 * @return A {@link java.net.HttpURLConnection}
	 * @throws IOException If there was an error generating the {@link java.net.HttpURLConnection}.
	 * @throws InterruptedException If the user interrupted the request.
	 */
	private HttpURLConnection generateConnection(String method,
			URL url, String postData, Hashtable headers)
				throws IOException, InterruptedException {
		if(rateLimitKBPS > 0) {
			float kbpsSinceLastLoad = hostMemory.kbpsSinceLastLoadFor(url);
			log.i("Load speed from " + url.toString() + " : " + Float.toString(kbpsSinceLastLoad));
			if(kbpsSinceLastLoad > rateLimitKBPS) {
				log.i("Delaying load of " + StringUtils.quote(url.toString()) +
							", current KBPS " +
							StringUtils.quote(Float.toString(kbpsSinceLastLoad)));
				try {
					Thread.sleep(Browser.DEFAULT_SLEEP_TIME);
				} catch (InterruptedException e) {
					log.e(e);
					throw new IOException(e);
				}			
			}
		}
		log.i("Browser loading URL '" + url.toString() + "'");
		
		//HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection conn = (HttpURLConnection) (new URL(url.toString())).openConnection();	
		
		// Add generic Headers.
		addHeaderToConnection(conn, ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE);
		addHeaderToConnection(conn, ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE);
		addHeaderToConnection(conn, USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE);
		addHeaderToConnection(conn, REFERER_HEADER_NAME, url.toString()); // default to the current URL as referer.
		Enumeration headerNames = headers.keys();
		while(headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			String headerValue = (String) headers.get(headerName);
			addHeaderToConnection(conn, headerName, headerValue);
		}
		
		// Add cookies passed directly into cookie store. Very primitive.
		/*for(int i = 0; i < cookies.length ; i++) {
			cookieStore.put(cookies[i].getName(), cookies[i].getValue());
		}*/

		
		// Add cookie header to request.
		if(cookieStore.size() > 0) {
			updateCookieRequestHeader(conn);
		}
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(timeout * 1000);

		if(method.equals("POST")) {

			conn.setRequestMethod("POST");
			
			OutputStreamWriter writer = null;
			
			writer = new OutputStreamWriter(conn.getOutputStream());
			
			postData = postData == null ? "" : postData;
			log.i("Using posts: " + StringUtils.quote(postData));
			writer.write(postData);
			writer.flush();
			
		} else {
			conn.setRequestMethod(method);
		}
		
		
		return conn;
	}
	
	// Indebted to jcookie (http://jcookie.sourceforge.net/doc.html)
	private InputStream connectHandlingRedirectCookies(String method, URL url,
			String postData, Hashtable headers)
				throws IOException, InterruptedException {
		return connectHandlingRedirectCookies(method, url, postData, headers, new Vector());
	}
	
	//private InputStream connectHandlingRedirectCookies(/*HttpURLConnection conn, */Vector redirects_followed)
	private InputStream connectHandlingRedirectCookies(String method, URL url,
			String postData, Hashtable headers, Vector redirects_followed)
				throws IOException, InterruptedException {
		HttpURLConnection conn = generateConnection(method, url, postData, headers);
		conn.setInstanceFollowRedirects(false);
		try {
			conn.connect();
			InputStream stream = conn.getInputStream();
			
			int code = conn.getResponseCode();
			
			log.i("Response code: " + Integer.toString(code));
			
			if(redirects_followed.size() <= MAX_REDIRECTS) {
				updateCookieStoreFromResponse(conn);
				if(code >= 300 && code < 400 ) {
					String redirect_string = conn.getHeaderField(LOCATION_HEADER_NAME);
					if(redirects_followed.contains(redirect_string)) {
						throw new IOException("Not following circular redirect from " +
								conn.getURL().toString() + " to " + redirect_string);
					} else {
						redirects_followed.addElement(redirect_string);
					}
					
					log.i("Following redirect #"
						+ Integer.toString(redirects_followed.size()) + " from " + conn.getURL().toString()
						+ " to " + redirect_string);
					
					try {
						stream.close();
						conn.disconnect();
						return connectHandlingRedirectCookies("GET",
								new URI(url.toString()).resolve(redirect_string).toURL(), null,
								headers, redirects_followed);
						
					} catch(Exception e) {
						throw new IOException("Unable to parse redirect from " + conn.getURL().toString()
								+ " to " + redirect_string + " (" + e.getMessage() + ")");
					} finally {
						stream.close();
						conn.disconnect();
					}
				} else if(code != SUCCESS_CODE) {
					throw new IOException("Can't deal with this response code (" + code + ").");
				}
			} else {
				throw new IOException("Max redirects exhausted.");
			}
			return stream;
		} catch(SocketTimeoutException e) {
			throw new IOException("Socket timeout after " + conn.getReadTimeout() + " seconds " +
					", " + e.bytesTransferred + " bytes read.");
		}
	}
	
	/**
	 * Very primitive handling of cookies.  Simply overwrites duplicate names in the CookieStore, ignoring host & path.
	 * @param conn
	 */
	private void updateCookieStoreFromResponse(HttpURLConnection conn) {
		String header_name, header_value;
		for(int i = 0 ; (header_name = conn.getHeaderFieldKey(i)) != null || i == 0 ; i++) {
			if(header_name == null) continue; // A mess, but sometimes the first header is null.
			if(header_name.equals("Set-Cookie") || header_name.equals("Set-Cookie2")) {
				header_value = conn.getHeaderField(i);
				int equals_loc = header_value.indexOf('=');
				if(equals_loc != -1) {
					String name = header_value.substring(0, equals_loc);
					String payload[] = StringUtils.split(header_value.substring(equals_loc + 1), "; ");
					String value = payload[0];
					
					log.i("Storing cookie '" + name + "' with value '" + value + "'");
					cookieStore.put(name, value);
				}
			}
		}
	}
	
	/**
	 * Bring the <code>Cookie</code> header of <code>conn</code> into line with {@link #cookieStore}.
	 * @param conn
	 */
	private void updateCookieRequestHeader(HttpURLConnection conn) {
		String[] cookieAry = new String[cookieStore.size()];
		Enumeration e = cookieStore.keys();
		int i = 0;
		while(e.hasMoreElements()) {
			String name = (String) e.nextElement();
			cookieAry[i] = name + '=' + (String) cookieStore.get(name);// + "; ";
			i++;
		}
		String cookieString = StringUtils.join(cookieAry, "; ");
		log.i("Using cookies: " + cookieString);
		conn.setRequestProperty("Cookie", cookieString);
	}
	
	private static class HostMemory {

		private static class LoadedFromHost {
			public final String host;
			public final Date timestamp;
			public final int bytesLoaded;
			public LoadedFromHost(String host, int bytesLoaded) {
				this.timestamp = new Date();
				this.host = host;
				this.bytesLoaded = bytesLoaded;
			}
		}
		
		private final Hashtable hostMemory = new Hashtable();
		public void add(URL url, int bytesLoaded) {
			LoadedFromHost loadedFromHost = new LoadedFromHost(
					url.getHost(), bytesLoaded);
			hostMemory.put(loadedFromHost.host, loadedFromHost);
		}
		public void add(String host, int bytesLoaded) {
			LoadedFromHost loadedFromHost = new LoadedFromHost(
					host, bytesLoaded);
			hostMemory.put(loadedFromHost.host, loadedFromHost);
		}
		public float kbpsSinceLastLoadFor(URL url) {
			Date now = new Date();
			String host = url.getHost();
			if(hostMemory.containsKey(host)) {
				LoadedFromHost lastLoad = (LoadedFromHost) hostMemory.get(host);
				long millisecondsSince = now.getTime() - lastLoad.timestamp.getTime() + 1;
				return lastLoad.bytesLoaded / millisecondsSince;
			} else {
				return 0;
			}
		}
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
	
	/**
	 * TODO Currently very primitive, just ignores URL.
	 */
	public void addCookies(Cookie[] cookies) {
		for(int i = 0 ; i < cookies.length ; i ++) {
			this.cookieStore.put(cookies[i].getName(), cookies[i].getValue());
		}
	}
}
