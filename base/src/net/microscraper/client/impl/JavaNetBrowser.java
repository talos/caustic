package net.microscraper.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Utils;

/**
 * This is a very, very lightweight browser.  It will add headers & posts, and do -very- primitive cookie handling.  It only
 * looks at the name & value of cookies -- it never checks expiration, or any other properties!
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	public String load(String url)
			throws InterruptedException, BrowserException {
		return load(url, new Hashtable(), new Hashtable(), new Hashtable(), new Pattern[] {});
	}
	private final Hashtable cookie_store = new Hashtable();
	private final Hashtable host_name_starts = new Hashtable();
	private final Hashtable host_name_amount_downloaded = new Hashtable();

	public String load(String url_string, Hashtable posts,
			Hashtable headers, Hashtable cookies,
			Pattern[] terminates)
			throws BrowserException, InterruptedException {
		
		OutputStreamWriter writer = null;
		InputStream stream = null;
		URL url;
		try {
			url = new URL(url_string);
		} catch(MalformedURLException e) {
			throw new BrowserException(url_string, e);
		}
		 
		try {
			Client.log.i("Browser loading URL '" + url.toString() + "'");
			
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(TIMEOUT);

			// Add cookies passed directly into cookie store. Very primitive.
			Utils.hashtableIntoHashtable(cookies, cookie_store);
			
			// Add generic Headers.
			Hashtable default_headers = new Hashtable();
			default_headers.put(ACCEPT_HEADER_NAME, ACCEPT_HEADER_DEFAULT_VALUE);
			default_headers.put(ACCEPT_LANGUAGE_HEADER_NAME, ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE);
			default_headers.put(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_DEFAULT_VALUE);
			default_headers.put(REFERER_HEADER_NAME, url.toString());
			addHeaders(conn, default_headers);
			addHeaders(conn, headers);
			
			// Add cookie headers.
			if(cookies.size() > 0) {
				String cookie_string = "";
				Enumeration e = cookies.keys();
				while(e.hasMoreElements()) {
					String key = (String) e.nextElement();
					cookie_string += URLEncoder.encode(key, ENCODING) + '=' + URLEncoder.encode((String) cookies.get(key), ENCODING) + "; ";
				}
				Client.log.i("Using cookies: " + cookie_string);
				conn.setRequestProperty("Cookie", cookie_string);
			}
			
			// Add Posts & set request type
			if(posts.size() > 0) {
				String post_data = "";
				Enumeration e = posts.keys();
				while(e.hasMoreElements()) {
					String key = (String) e.nextElement();
					post_data += URLEncoder.encode(key, ENCODING) + '=' + URLEncoder.encode((String) posts.get(key), ENCODING) + '&';
				}
				post_data = post_data.substring(0, post_data.length() -1); // trim trailing ampersand

				conn.setRequestMethod("POST");
				writer = new OutputStreamWriter(conn.getOutputStream());
				
				writer.write(post_data);
				writer.flush();
			} else {
				conn.setRequestMethod("GET");
			}
			
			Client.log.i("Waiting for " + conn.getRequestMethod()
					+ " response from " + url.toString() + "...");
			connectHandlingRedirectCookies(conn);
			
			Client.log.i("Loading " + url.toString() + "...");
			// Pull response.
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			//reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			stream = conn.getInputStream();
			String content_string;
			byte[] buffer = new byte[512];
			int readBytes;
			loading: while((readBytes = stream.read(buffer)) != -1) {
				if(Thread.interrupted()) {
					throw new InterruptedException("Interrupted loading of " + url.toString());
				}
				
				content.write(buffer, 0, readBytes);
				content_string = new String(content.toByteArray());
				for(int i = 0 ; i < terminates.length ; i++) {
					if(terminates[i].matches(content_string)){
						Client.log.i("Terminating " + url.toString() + " due to pattern " + terminates[i].toString());
						stream.close();
						break loading;
					}
				}
			}
			content_string = content.toString();
			return content_string;
		} catch(IOException e) {
			throw new BrowserException(url_string, e);
		}
	}
	
	private static void addHeaders(HttpURLConnection conn, Hashtable headers)  {
		Enumeration e = headers.keys();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			conn.setRequestProperty(key, (String) headers.get(key));
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
				
				Client.log.i("Following redirect #"
					+ Integer.toString(redirects_followed.size()) + " from " + conn.getURL().toString()
					+ " to " + redirect_string);
				
				URI new_uri;
				try {
					new_uri = new URI(conn.getURL().toString()).resolve(redirect_string);

				} catch(Exception e) {
					throw new IOException("Unable to parse redirect from " + conn.getURL().toString()
							+ " to " + redirect_string + " (" + e.getMessage() + ")");
				}
				stream.close();
				conn.disconnect();
				return connectHandlingRedirectCookies(
						(HttpURLConnection) new_uri.toURL().openConnection(), redirects_followed);
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
					Client.log.i("Storing cookie '" + name + "' with value '" + value + "'");
					cookie_store.put(name, value);
				}
			}
		}
	}
}
