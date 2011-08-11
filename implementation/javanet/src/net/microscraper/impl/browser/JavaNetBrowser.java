package net.microscraper.impl.browser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.BasicNameValuePair;
import net.microscraper.Log;
import net.microscraper.NameValuePair;
import net.microscraper.Utils;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.regexp.PatternInterface;

/**
 * This is a very lightweight {@link Browser}.  It will add headers & posts, and does <b>very</b>
 *  primitive cookie handling.  It only
 * looks at the name & value of cookies -- not expiration or domains.
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	private static final String encoding = UTF_8;
	private final Log log;
	private final Hashtable cookieStore = new Hashtable();
	private final HostMemory hostMemory = new HostMemory();
	private final int maxKBPS;
	private final int timeout;
	private final int maxResponseSize;
	/**
	 * 
	 * @param log The {@link Log} to send messages to.
	 * @param maxKBPS The maximum average number of kilobytes per second that can be loaded
	 * from a single host before another request is made.
	 * @param timeout How many seconds before giving up on a request.
	 * @param maxResponseSize The maximum size of a response in KB that this {@link Browser}
	 * will load before terminating.  Since responses are fed straight through to a regex
	 * parser, it is wise not to deal with huge pages.
	 */
	public JavaNetBrowser(Log log, int maxKBPS, int timeout, int maxResponseSize) {
		this.log = log;
		this.maxKBPS = maxKBPS;
		this.timeout = timeout;
		this.maxResponseSize = maxResponseSize;
	}
	
	public void head(boolean useRateLimit, String url, NameValuePair[] headers, NameValuePair[] cookies)
			throws BrowserException {
		log.i("Retrieving Head from  " + url.toString() + "...");
		try {
			//HttpURLConnection conn = generateConnection(rateLimit, url, headers, cookies);
			//conn.setRequestMethod("HEAD");
			connectHandlingRedirectCookies(useRateLimit, "HEAD", new URL(url.toString()), null, headers, cookies);
		} catch (IOException e) {
			throw new BrowserException(url, e);
		}
	}

	public String get(boolean useRateLimit, String url, NameValuePair[] headers,
			NameValuePair[] cookies, PatternInterface[] terminates) throws
			BrowserException {
		log.i("Getting  " + url.toString() + "...");
		try {
			InputStream stream = connectHandlingRedirectCookies(useRateLimit, "GET", new URL(url.toString()), null, headers, cookies);
			return pullResponse(url, stream, terminates);
		} catch(IOException e) {
			throw new BrowserException(url, e);
		}
	}

	public String post(boolean useRateLimit, String url, NameValuePair[] headers, NameValuePair[] cookies,
			PatternInterface[] terminates, NameValuePair[] posts)
				throws BrowserException {
		log.i("Posting to  " + url.toString() + "...");
		try {			
			InputStream stream = connectHandlingRedirectCookies(useRateLimit, "POST", new URL(url.toString()), posts, headers, cookies);
			return pullResponse(url, stream, terminates);
		} catch(IOException e) {
			throw new BrowserException(url, e);
		}
	}
	
	/**
	 * Get the body of the response using an already-primed HttpURLConnection.  Can be terminated prematurely
	 * by a pattern.
	 * @param conn a connection ready for {@link java.net.URLConnection#getInputStream}.
	 * @param terminates array of {@link PatternInterface}s to interrupt the load. Can be <code>null</code>.
	 * @return the response body.
	 * @throws BrowserException if there was an exception loading, including user interrupt.
	 */
	private String pullResponse(String url, InputStream stream, PatternInterface[] terminates)
			throws BrowserException {

		//URL url = conn.getURL();
		String responseBody;
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		try {
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
					log.i("Have loaded " + totalReadBytes + " bytes from " + Utils.quote(url));
					lastTotalReadBytes = totalReadBytes;
				}
				content.write(buffer, 0, readBytes);
				responseBody = new String(content.toByteArray());
				if(totalReadBytes > maxResponseSize * 1024) {
					throw new IOException("Exceeded maximum response size of " + maxResponseSize + "KB.");
				}
				if(terminates != null && terminates.length > 0) {
					for(int i = 0 ; i < terminates.length ; i++) {
						if(terminates[i].matches(responseBody)){
							log.i("Terminating " + url.toString() + " due to pattern " + terminates[i].toString());
							break loading;
						}
					}
				}
			}
			stream.close();
		} catch(IOException e) {
			throw new BrowserException(url, e);
		} catch(InterruptedException e) {
			throw new BrowserException(url, e);
		}
		responseBody = content.toString();
		try {
			hostMemory.add(new URL(url.toString()), responseBody.length());
		} catch (MalformedURLException e) {
			throw new BrowserException(url, e);
		}
		log.i("Response body: " + responseBody);
		return responseBody;
	}
	
	private static void addHeaderToConnection(HttpURLConnection conn, NameValuePair header) {
		conn.setRequestProperty(header.getName(), header.getValue());
	}
	
	/**
	 * Generate an {@link java.net.HttpURLConnection}.  Defaults the Referer header to the current url.
	 * @param useRateLimit Whether to throw {@link BrowserDelayException} to avoid loading too much from a host.
	 * @param url A {@link URL} to load.  Also defaults to be the Referer in the request header.
	 * @param headers An array of {@link NameValuePair}s, can be <code>null</code>
	 * @param cookies An array of {@link NameValuePair}s, can be <code>null</code>
	 * @return A {@link java.net.HttpURLConnection}
	 * @throws IOException If there was an error generating the {@link java.net.HttpURLConnection}.
	 * @throws BrowserDelayException If this {@link Browser} is averaging more kilobytes per second from this
	 * host than allowed at instantiation.
	 */
	private HttpURLConnection generateConnection(boolean useRateLimit, String method,
			URL url, NameValuePair[] posts,
			NameValuePair[] headers, NameValuePair[] cookies)
				throws IOException, BrowserException {
		if(useRateLimit == true) {
			float kbpsSinceLastLoad = hostMemory.kbpsSinceLastLoadFor(url);
			log.i("Load speed from " + url.toString() + " : " + Float.toString(kbpsSinceLastLoad));
			if(kbpsSinceLastLoad > maxKBPS) {
				log.i("Delaying load of " + Utils.quote(url.toString()) +
							", current KBPS " +
							Utils.quote(Float.toString(kbpsSinceLastLoad)));
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
		addHeaderToConnection(conn, new BasicNameValuePair(ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new BasicNameValuePair(ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new BasicNameValuePair(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new BasicNameValuePair(REFERER_HEADER_NAME, url.toString())); // default to the current URL as referer.
		if(headers != null) {
			for(int i = 0 ; i < headers.length ; i++) {
				addHeaderToConnection(conn, headers[i]);
			}
		}
		
		// Add cookies passed directly into cookie store. Very primitive.
		if(cookies != null) {
			for(int i = 0; i < cookies.length ; i++) {
				cookieStore.put(cookies[i].getName(), cookies[i].getValue());
			}
		}

		
		// Add cookie header to request.
		if(cookieStore.size() > 0) {
			updateCookieRequestHeader(conn);
		}
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(timeout * 1000);

		if(method.equals("POST")) {

			String post_data = "";
			if(posts != null) {
				for(int i = 0 ; i < posts.length ; i ++) {
					post_data += encode(posts[i].getName(), encoding) + '=' + encode(posts[i].getValue(), encoding) + '&';
				}
				post_data = post_data.substring(0, post_data.length() -1); // trim trailing ampersand
				log.i("Using posts: " + post_data);
			}
			
			conn.setRequestMethod("POST");
			
			OutputStreamWriter writer = null;
			
			writer = new OutputStreamWriter(conn.getOutputStream());
			
			writer.write(post_data);
			writer.flush();
			
		} else {
			conn.setRequestMethod(method);
		}
		
		
		return conn;
	}
	
	// Indebted to jcookie (http://jcookie.sourceforge.net/doc.html)
	//private InputStream connectHandlingRedirectCookies(HttpURLConnection conn)
	private InputStream connectHandlingRedirectCookies(boolean useRateLimit, String method, URL url,
			NameValuePair[] posts,
			NameValuePair[] headers, NameValuePair[] cookies)
				throws IOException, BrowserException {
		return connectHandlingRedirectCookies(useRateLimit, method, url, posts, headers, cookies, new Vector());
	}
	
	//private InputStream connectHandlingRedirectCookies(/*HttpURLConnection conn, */Vector redirects_followed)
	private InputStream connectHandlingRedirectCookies(boolean useRateLimit, String method, URL url,
			NameValuePair[] posts,
			NameValuePair[] headers, NameValuePair[] cookies, Vector redirects_followed)
				throws IOException, BrowserException {
		HttpURLConnection conn = generateConnection(useRateLimit, method, url, posts, headers, cookies);
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
						return connectHandlingRedirectCookies(useRateLimit, "GET",
								new URI(url.toString()).resolve(redirect_string).toURL(), null,
								headers, null, redirects_followed);
						
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
					String payload[] = Utils.split(header_value.substring(equals_loc + 1), "; ");
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
		//String cookie_string = "";
		String[] cookieAry = new String[cookieStore.size()];
		Enumeration e = cookieStore.keys();
		int i = 0;
		while(e.hasMoreElements()) {
			String name = (String) e.nextElement();
			cookieAry[i] = name + '=' + (String) cookieStore.get(name);// + "; ";
			i++;
		}
		String cookieString = Utils.join(cookieAry, "; ");
		log.i("Using cookies: " + cookieString);
		conn.setRequestProperty("Cookie", cookieString);
	}
	
	private static class HostMemory {

		private static class LoadedFromHost {
			public final String host;
			public final Date timestamp;
			public final int bytesLoaded;
			public LoadedFromHost(URL url, int bytesLoaded) {
				this.timestamp = new Date();
				this.host = url.getHost();
				this.bytesLoaded = bytesLoaded;
			}
		}
		
		private final Hashtable hostMemory = new Hashtable();
		public void add(URL url, int bytesLoaded) {
			LoadedFromHost loadedFromHost = new LoadedFromHost(
					url, bytesLoaded);
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
	

	/*private String decode(String string) throws UnsupportedEncodingException {
		return URLDecoder.decode(string, encoding);
	}*/

	public String encode(String stringToEncode, String encoding)
			throws BrowserException {
		try {
			return URLEncoder.encode(stringToEncode, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new BrowserException(stringToEncode, e);
		}
	}
}
