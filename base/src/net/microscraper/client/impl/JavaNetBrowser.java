package net.microscraper.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Log;
import net.microscraper.client.UnencodedNameValuePair;

/**
 * This is a very, very lightweight browser.  It will add headers & posts, and do -very- primitive cookie handling.  It only
 * looks at the name & value of cookies -- it never checks expiration, or any other properties!
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	private final Log log;
	private final Hashtable cookieStore = new Hashtable();
	public JavaNetBrowser(Log log) {
		this.log = log;
	}
	/*
	public Interfaces.JSON.Object loadJSON(String url, Interfaces.JSON jsonInterface)
			throws InterruptedException, BrowserException, JSONInterfaceException {
		Hashtable jsonHeaders = new Hashtable();
		jsonHeaders.put(ACCEPT_HEADER_NAME, ACCEPT_HEADER_JSON_VALUE);
		String jsonString = load(url, new Hashtable(), jsonHeaders, new Hashtable(), new Pattern[] {});
		log.i(jsonString);
		return jsonInterface.getTokener(jsonString).nextValue();
	}*/
	//private final Hashtable host_name_starts = new Hashtable();
	//private final Hashtable host_name_amount_downloaded = new Hashtable();
	
	public void head(URL url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies)
			throws DelayRequest, BrowserException {
		log.i("Retrieving Head from  " + url.toString() + "...");
		try {
			HttpURLConnection conn = generateConnection(url, headers, cookies);
			conn.setRequestMethod("HEAD");
			connectHandlingRedirectCookies(conn);
		} catch (IOException e) {
			throw new BrowserException(url, e);
		}
	}

	public String get(URL url, UnencodedNameValuePair[] headers,
			EncodedNameValuePair[] cookies, Pattern[] terminates) throws DelayRequest,
			BrowserException {
		log.i("Getting  " + url.toString() + "...");
		try {
			HttpURLConnection conn = generateConnection(url, headers, cookies);
			conn.setRequestMethod("GET");
			connectHandlingRedirectCookies(conn);
			return pullResponse(conn, terminates);
		} catch(IOException e) {
			throw new BrowserException(url, e);
		}
	}

	public String post(URL url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies,
			Pattern[] terminates, EncodedNameValuePair[] posts)
				throws DelayRequest, BrowserException {
		log.i("Posting to  " + url.toString() + "...");
		try {
			HttpURLConnection conn = generateConnection(url, headers, cookies);
			
			String post_data = "";
			for(int i = 0 ; i < posts.length ; i ++) {
				post_data += posts[i].getName() + '=' + posts[i].getValue() + '&';
			}
			post_data = post_data.substring(0, post_data.length() -1); // trim trailing ampersand
	
			conn.setRequestMethod("POST");
			
			OutputStreamWriter writer = null;
			
			writer = new OutputStreamWriter(conn.getOutputStream());
			
			writer.write(post_data);
			writer.flush();
			
			connectHandlingRedirectCookies(conn);
			return pullResponse(conn, terminates);
		} catch(IOException e) {
			throw new BrowserException(url, e);
		}
	}
	
	/**
	 * Get the body of the response using an already-primed HttpURLConnection.  Can be terminated prematurely
	 * by a pattern.
	 * @param conn a connection ready for #getInputStream.
	 * @param terminates array of patterns to interrupt the load.
	 * @return the response body.
	 * @throws BrowserException if there was an exception loading, including user interrupt.
	 */
	private String pullResponse(HttpURLConnection conn, Pattern[] terminates)
			throws BrowserException {

		URL url = conn.getURL();
		try {
			// Pull response.
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			InputStream stream = conn.getInputStream();
			String responseBody;
			byte[] buffer = new byte[512];
			int readBytes;
			loading: while((readBytes = stream.read(buffer)) != -1) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				
				content.write(buffer, 0, readBytes);
				responseBody = new String(content.toByteArray());
				for(int i = 0 ; i < terminates.length ; i++) {
					if(terminates[i].matches(responseBody)){
						log.i("Terminating " + conn.getURL().toString() + " due to pattern " + terminates[i].toString());
						stream.close();
						break loading;
					}
				}
			}
			responseBody = content.toString();
			return responseBody;
		} catch(IOException e) {
			throw new BrowserException(url, e);
		} catch(InterruptedException e) {
			throw new BrowserException(url, e);
		}
	}
	
	private static void addHeaderToConnection(HttpURLConnection conn, UnencodedNameValuePair header) {
		conn.setRequestProperty(header.getName(), header.getValue());
	}
	
	private HttpURLConnection generateConnection(URL url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies)
				throws IOException {
		log.i("Browser loading URL '" + url.toString() + "'");
		
		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();			
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(TIMEOUT);
		
		// Add cookies passed directly into cookie store. Very primitive.
		for(int i = 0; i < cookies.length ; i++) {
			cookieStore.put(cookies[i].getName(), cookies[i].getValue());
		}
		
		// Add generic Headers.
		addHeaderToConnection(conn, new UnencodedNameValuePair(ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new UnencodedNameValuePair(ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new UnencodedNameValuePair(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE));
		addHeaderToConnection(conn, new UnencodedNameValuePair(REFERER_HEADER_NAME, url.toString()));
		for(int i = 0 ; i < headers.length ; i++) {
			addHeaderToConnection(conn, headers[i]);
		}
		
		// Add cookie headers.
		if(cookieStore.size() > 0) {
			String cookie_string = "";
			Enumeration e = cookieStore.keys();
			while(e.hasMoreElements()) {
				String name = (String) e.nextElement();
				cookie_string += name + '=' + (String) cookieStore.get(name) + "; ";
			}
			log.i("Using cookies: " + cookie_string);
			conn.setRequestProperty("Cookie", cookie_string);
		}
		
		return conn;
	}
	
	// Indebted to jcookie (http://jcookie.sourceforge.net/doc.html)
	private InputStream connectHandlingRedirectCookies(HttpURLConnection conn)
				throws IOException {
		return connectHandlingRedirectCookies(conn, new Vector());
	}
	private InputStream connectHandlingRedirectCookies(HttpURLConnection conn, Vector redirects_followed)
				throws IOException {
		conn.setInstanceFollowRedirects(false);
		conn.connect();
		InputStream stream = conn.getInputStream();
		int code = conn.getResponseCode();
		
		if(redirects_followed.size() <= MAX_REDIRECTS) {
			updateCookieStore(conn);
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
				
				URI newURI;
				try {
					newURI = new URI(conn.getURL().toString()).resolve(redirect_string);

				} catch(Exception e) {
					throw new IOException("Unable to parse redirect from " + conn.getURL().toString()
							+ " to " + redirect_string + " (" + e.getMessage() + ")");
				}
				stream.close();
				conn.disconnect();
				return connectHandlingRedirectCookies(
						(HttpURLConnection) newURI.toURL().openConnection(), redirects_followed);
			} else if(code != SUCCESS_CODE) {
				throw new IOException("Can't deal with this response code (" + code + ").");
			}
		} else {
			throw new IOException("Max redirects exhausted.");
		}
		return stream;
	}
	
	/**
	 * Very primitive handling of cookies.  Simply overwrites duplicate names in the CookieStore, ignoring host & path.
	 * @param conn
	 */
	private void updateCookieStore(HttpURLConnection conn) {
		String header_name, header_value;
		for(int i = 0 ; (header_name = conn.getHeaderFieldKey(i)) != null || i == 0 ; i++) {
			if(header_name == null) continue; // A mess, but sometimes the first header is null.
			if(header_name.equals("Set-Cookie") || header_name.equals("Set-Cookie2")) {
				header_value = conn.getHeaderField(i);
				int equals_loc = header_value.indexOf('=');
				if(equals_loc != -1) {
					String name = header_value.substring(0, equals_loc);
					String value = header_value.substring(equals_loc + 1);
					log.i("Storing cookie '" + name + "' with value '" + value + "'");
					cookieStore.put(name, value);
				}
			}
		}
	}
}
