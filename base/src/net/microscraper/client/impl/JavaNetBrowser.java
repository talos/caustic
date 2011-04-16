package net.microscraper.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.Result;
import net.microscraper.database.schema.AbstractHeader;

/**
 * This is a very, very lightweight browser.  It will add headers & posts, and do -very- primitive cookie handling.  It only
 * looks at the name & value of cookies -- it never checks expiration, or any other properties!
 * @author john
 *
 */
public class JavaNetBrowser implements Browser {
	private static final String ENCODING = "UTF-8";
	public String load(String url, AbstractResult caller)
			throws InterruptedException, BrowserException, ResourceNotFoundException, TemplateException, MissingVariable {
		return load(url, new AbstractResource[] {}, new AbstractResource[] {},
				new AbstractResource[] {}, new AbstractResource[] {},
				caller);
	}
	private final Hashtable cookie_store = new Hashtable();

	// Thank you from http://www.exampledepot.com/egs/java.net/Post.html
	public String load(String url_string, AbstractResource[] posts,
			AbstractResource[] headers, AbstractResource[] cookies,
			AbstractResource[] terminates, AbstractResult caller)
			throws BrowserException, ResourceNotFoundException,
			TemplateException, MissingVariable, InterruptedException {
		
		OutputStreamWriter writer = null;
		InputStream stream = null;
		
		try {
			URL url = new URL(url_string);
			
			Client.context().log.i("Browser loading URL '" + url.toString() + "'");
			
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(TIMEOUT);

			// Add cookies passed directly into cookie store. Very primitive.
			for(int i = 0 ; i < cookies.length ; i ++) {
				Result r = cookies[i].getValue(caller)[0];
				cookie_store.put(r.key, r.value);
			}
			
			// Add generic Headers.
			addHeaders(conn, Browser.DEFAULT_HEADERS, caller);
			addHeaders(conn, new AbstractHeader[] {
				new AbstractHeader(Browser.REFERER_HEADER_NAME, url.toString())
			}, caller);
			addHeaders(conn, headers, caller);

			// Add cookie headers.
			if(cookies.length > 0) {
				String cookie_string = "";
				for(int i = 0 ; i < cookies.length ; i ++) {
					Result cookie_result = cookies[i].getValue(caller)[0];
					cookie_string += cookie_result.key + '=' + cookie_result.value + "; ";
				}
				conn.setRequestProperty("Cookie", cookie_string);
			}
			
			// Add Posts & set request type
			if(posts.length > 0) {
				conn.setRequestMethod("POST");
				writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(Utils.join(encodeNameValuePairs(posts, caller), "&"));
				writer.flush();
			} else {
				conn.setRequestMethod("GET");
			}
			
			// Set up patterns
			Pattern[] patterns = new Pattern[terminates.length];
			for(int i = 0; i < terminates.length; i++) {
				patterns[i] = Client.context().regexp.compile(terminates[i].getValue(caller)[0].value);
			}
			
			Client.context().log.i("Waiting for response from " + url.toString() + "...");
			//conn.connect();
			connectHandlingRedirectCookies(conn, 0);

			Client.context().log.i("Loading " + url.toString() + "...");
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
				for(int i = 0 ; i < patterns.length ; i++) {
					if(patterns[i].matches(content_string)){
						Client.context().log.i("Terminating " + url.toString() + " due to pattern " + terminates[i].toString());
						stream.close();
						break loading;
					}
				}
			}
			content_string = content.toString();
			return content_string;
		} catch(MalformedURLException e) {
			throw new BrowserException(url_string + " is not a well-formed URL.");
		} catch(IOException e) {
			throw new BrowserException("Error " + e.toString() + " loading " + url_string);
		}
	}
	
	private static void addHeaders(HttpURLConnection conn, AbstractResource[] headers, AbstractResult caller)
			throws ResourceNotFoundException, TemplateException, MissingVariable, BrowserException, InterruptedException {
		for(int i = 0 ; i < headers.length ; i ++) {
			Result r = headers[i].getValue(caller)[0];
			conn.setRequestProperty(r.key, r.value);
		}
	}
	
	private static String[] encodeNameValuePairs(AbstractResource[] posts, AbstractResult caller)
			throws ResourceNotFoundException, TemplateException, MissingVariable, BrowserException, InterruptedException, UnsupportedEncodingException {
		String[] post_strings = new String[posts.length];
		for(int i = 0 ; i < posts.length ; i ++ ) {
			Result r = posts[i].getValue(caller)[0];
			post_strings[i] =
				URLEncoder.encode(r.key, ENCODING) + "=" + URLEncoder.encode(r.value, ENCODING);
		}
		return post_strings;
	}
	
	// Indebted to jcookie (http://jcookie.sourceforge.net/doc.html)
	private InputStream connectHandlingRedirectCookies(HttpURLConnection conn, int redirects_followed) throws IOException {
		conn.setInstanceFollowRedirects(false);
		conn.connect();
		InputStream stream = conn.getInputStream();
		
		int code = conn.getResponseCode();
		
		if(redirects_followed <= MAX_REDIRECTS) {
			updateCookieStore(conn);
			if(code >= 300 && code < 400 ) {
				Client.context().log.i("Following redirect #" + Integer.toString(redirects_followed));
				stream.close();
				URL url = new URL(conn.getHeaderField(LOCATION_HEADER_NAME));
				conn.disconnect();
				
				return connectHandlingRedirectCookies((HttpURLConnection) url.openConnection(), redirects_followed + 1);
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
		for(int i = 0 ; (header_name = conn.getHeaderFieldKey(i)) != null ; i++) {
			if(header_name.equals("Set-Cookie") || header_name.equals("Set-Cookie2")) {
				header_value = conn.getHeaderField(i);
				int equals_loc = header_value.indexOf('=');
				if(equals_loc != -1) {
					String name = header_value.substring(0, equals_loc);
					String value = header_value.substring(equals_loc + 1);
					Client.context().log.i("Storing cookie '" + name + "' with value '" + value + "'");
					cookie_store.put(name, value);
				}
			}
		}
	}
}
