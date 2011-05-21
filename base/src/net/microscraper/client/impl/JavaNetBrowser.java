package net.microscraper.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.client.Log;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Utils;

/**
 * This is a very, very lightweight browser.  It will add headers & posts, and do -very- primitive cookie handling.  It only
 * looks at the name & value of cookies -- it never checks expiration, or any other properties!
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	private final Log log;
	private final Hashtable cookieStore = new Hashtable();
	private final HostMemory hostMemory = new HostMemory();
	private final int maxKBPS;
	//private final String encoding;
	public JavaNetBrowser(Log log, int maxKBPS) {
		this.log = log;
		this.maxKBPS = maxKBPS;
		//this.encoding = encoding;
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
			throws BrowserDelayException, BrowserException {
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
			EncodedNameValuePair[] cookies, PatternInterface[] terminates) throws BrowserDelayException,
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
			PatternInterface[] terminates, EncodedNameValuePair[] posts)
				throws BrowserDelayException, BrowserException {
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
	 * @throws BrowserDelayException if we have loaded too much too fast from a host.
	 */
	private String pullResponse(HttpURLConnection conn, PatternInterface[] terminates)
			throws BrowserException {

		URL url = conn.getURL();
		String responseBody;
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		try {
			// Pull response.
			InputStream stream = conn.getInputStream();
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
						break loading;
					}
				}
			}
			stream.close();
			conn.disconnect();
		} catch(IOException e) {
			throw new BrowserException(url, e);
		} catch(InterruptedException e) {
			throw new BrowserException(url, e);
		}
		responseBody = content.toString();
		hostMemory.add(url, responseBody.length());
		return responseBody;
	}
	
	private static void addHeaderToConnection(HttpURLConnection conn, UnencodedNameValuePair header) {
		conn.setRequestProperty(header.getName(), header.getValue());
	}
	
	private HttpURLConnection generateConnection(URL url, UnencodedNameValuePair[] headers, EncodedNameValuePair[] cookies)
				throws IOException, BrowserDelayException {
		float kbpsSinceLastLoad = hostMemory.kbpsSinceLastLoadFor(url);
		//log.i("Load speed from " + url.toString() + " : " + Float.toString(kbpsSinceLastLoad));
		if(kbpsSinceLastLoad > maxKBPS) {
			throw new BrowserDelayException(url, kbpsSinceLastLoad);
		} else {
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
			//addHeaderToConnection(conn, new UnencodedNameValuePair(REFERER_HEADER_NAME, url.toString()));
			for(int i = 0 ; i < headers.length ; i++) {
				addHeaderToConnection(conn, headers[i]);
			}
			
			// Add cookie headers.
			if(cookieStore.size() > 0) {
				//String cookie_string = "";
				String[] cookieAry = new String[cookieStore.size()];
				Enumeration e = cookieStore.keys();
				int i = 0;
				while(e.hasMoreElements()) {
					String name = (String) e.nextElement();
					cookieAry[i] = name + '=' + (String) cookieStore.get(name);// + "; ";
				}
				String cookieString = Utils.join(cookieAry, "; ");
				log.i("Using cookies: " + cookieString);
				conn.setRequestProperty("Cookie", cookieString);
			}
			
			return conn;
		}
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
					String payload[] = Utils.split(header_value.substring(equals_loc + 1), "; ");
					String value = payload[0];
					
					log.i("Storing cookie '" + name + "' with value '" + value + "'");
					cookieStore.put(name, value);
				}
			}
		}
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
				long millisecondsSince = now.getTime() - lastLoad.timestamp.getTime();
				return lastLoad.bytesLoaded / millisecondsSince;
			} else {
				return 0;
			}
		}
	}
}
