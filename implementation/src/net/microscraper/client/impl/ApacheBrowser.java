package net.microscraper.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.microscraper.client.Browser;
import net.microscraper.client.Client;
import net.microscraper.database.schema.AbstractHeader;
import net.microscraper.database.schema.WebPage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


//public class ApacheHttpInterface implements HttpInterface {
public class ApacheBrowser implements Browser {
	private final BasicCookieStore cookie_store = new BasicCookieStore();
	private final HttpParams http_params = new BasicHttpParams();
	private final DefaultHttpClient http_client = new DefaultHttpClient();

	public ApacheBrowser() {
		http_params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		http_params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		http_params.setParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, false);
		http_params.setParameter(ClientPNames.MAX_REDIRECTS, 100);
		
		http_client.setCookieStore(cookie_store);
		http_client.setParams(http_params);
	}
	
	@Override
	public String load(WebPage web_page) throws InterruptedException,
			BrowserException {
		try {
			URI uri = new URI(web_page.url);
			
			AbstractHeader[] posts = web_page.posts;
			AbstractHeader[] cookies = web_page.cookies;
			AbstractHeader[] headers = web_page.headers;
			
			// Set up our httpclient to handle 302 redirects properly.
			http_client.setRedirectHandler(new RedirectHandler(uri));
			
			HttpRequestBase http_request;
			
			// Add posts.
			if(posts.length > 0) {
				HttpPost http_post = new HttpPost(uri);
				
				http_post.setEntity(generateFormEntity(posts));
				http_request = http_post;
			} else {
				http_request = new HttpGet(uri);
			}
			
			// Add headers.
			for(int i = 0 ; i < headers.length ; i ++) {
				http_request.addHeader(headers[i].name, headers[i].value);
			}

			// Add cookies.
			for(int i = 0 ; i < cookies.length ; i ++) {
				BasicClientCookie cookie = new BasicClientCookie(cookies[i].name, cookies[i].value);
				cookie.setDomain(uri.getHost());
				cookie_store.addCookie(cookie);
			}
			
			HttpResponse response = http_client.execute(http_request);

			StatusLine status = response.getStatusLine();
			
			if(status.getStatusCode() == 200) {
				//return response.getEntity();
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				byte[] buffer = new byte[512];
				while(stream.read(buffer) != -1) {
					Client.context().log.i(new String(buffer));
				}
			} else {
				throw new HttpResponseException(status.getStatusCode(), "Unable to get content: " + Integer.toString(status.getStatusCode()));
			}

		} catch(URISyntaxException e) {
			throw new BrowserException(e.toString());
		}
	}
	
	/**
	 * Private method, converts an array of AbstractHeaders to an UrlEncodedFormEntity.
	 * @param headers.
	 * @throws UnsupportedEncodingException If the Map contains a string that cannot be encoded.
	 */
	private static UrlEncodedFormEntity generateFormEntity(AbstractHeader[] headers)
				throws UnsupportedEncodingException {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		for(int i = 0; i < headers.length ; i ++) {
			pairs.add(new BasicNameValuePair(headers[i].name, headers[i].value));
		}
		return new UrlEncodedFormEntity(pairs);
	}
	
	// Force handling of 302s.  Default implementation does not handle them
	// with POST requests.
	private static class RedirectHandler extends DefaultRedirectHandler {
		private final URI uri;
		public RedirectHandler(URI _uri) {
			uri = _uri;
		}
		@Override
		public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
			if(response.getStatusLine().getStatusCode() == 302) {
				return true;
			} else {
				return super.isRedirectRequested(response, context);
			}
		}
		
		// Only use the overriden method with 302s.
		@Override
		public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
			if(response.getStatusLine().getStatusCode() != 302) {
				return super.getLocationURI(response, context);
			}
			String redirect = response.getFirstHeader("Location").getValue();
			
			return uri.resolve(redirect);
		}
		
	}
}
