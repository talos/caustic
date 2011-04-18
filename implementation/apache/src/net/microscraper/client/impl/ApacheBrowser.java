package net.microscraper.client.impl;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.microscraper.client.Browser;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.Result;
import net.microscraper.database.schema.AbstractHeader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;


public class ApacheBrowser implements Browser {
	//public static final boolean USE_CACHE = true;
	//public static final boolean DO_NOT_USE_CACHE = false;
	
	private final BasicCookieStore cookie_store = new BasicCookieStore();
	private final HttpParams http_params = new BasicHttpParams();
	
	//private final HashMap<WebPage, String> cache = new HashMap<WebPage, String>();
	//private final boolean use_cache;
	
	public ApacheBrowser(/*boolean _use_cache*/) {
		//use_cache = _use_cache;
		http_params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		http_params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		http_params.setParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, false);
		http_params.setParameter(ClientPNames.MAX_REDIRECTS, MAX_REDIRECTS);
		http_params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
		http_params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);
		http_params.setLongParameter(ConnManagerPNames.TIMEOUT, TIMEOUT);
	}
	
	@Override
	public String load(String url) throws InterruptedException,
			BrowserException, ResourceNotFoundException, TemplateException, MissingVariable {
		return load(url, new AbstractResource[] {}, new AbstractResource[] {}, new AbstractResource[] {}, new AbstractResource[] {}, caller);
		
	}
	
	@Override
	public String load(String url, AbstractResource[] posts, AbstractResource[] headers,
			AbstractResource[] cookies, AbstractResource[] terminates, AbstractResult caller)
			throws BrowserException, ResourceNotFoundException, TemplateException, MissingVariable, InterruptedException {
		/*if(cache.containsKey(web_page) && use_cache == true) {
			Client.context().log.i("Caught in cache");
			return cache.get(web_page);
		}*/
		try {
			URI uri = new URI(url);
			
			Client.context().log.i("Browser loading URL '" + uri.toString() + "'");
			
			// Set up our HttpClient
			DefaultHttpClient http_client = new DefaultHttpClient();
			http_client.setCookieStore(cookie_store);
			http_client.setParams(http_params);
			
			// Set up our httpclient to handle 302 redirects properly.
			http_client.setRedirectHandler(new RedirectHandler(uri));
			
			HttpRequestBase http_request;
			
			// Add posts.
			if(posts.length > 0) {
				HttpPost http_post = new HttpPost(uri);
				
				http_post.setEntity(generateFormEntity(posts, caller));
				http_request = http_post;
			} else {
				http_request = new HttpGet(uri);
			}
			
			// Add headers.
			addHeaders(http_request, Browser.DEFAULT_HEADERS, caller);
			addHeaders(http_request, new AbstractHeader[] {
				new AbstractHeader(Browser.REFERER_HEADER_NAME, uri.toString())	
			}, caller);
			addHeaders(http_request, headers, caller);
						
			// Add cookies.
			for(int i = 0 ; i < cookies.length ; i ++) {
				Result cookie_result = cookies[i].getValue(caller)[0];
				BasicClientCookie cookie = new BasicClientCookie(cookie_result.key, cookie_result.value);
				cookie.setDomain(uri.getHost());
				cookie_store.addCookie(cookie);
			}
			
			// Set up patterns
			Pattern[] patterns = new Pattern[terminates.length];
			for(int i = 0; i < terminates.length; i++) {
				patterns[i] = Client.context().regexp.compile(terminates[i].getValue(caller)[0].value);
			}

			Client.context().log.i("Waiting for response from " + uri.toString() + "...");
			HttpResponse response = http_client.execute(http_request);
			
			StatusLine status = response.getStatusLine();
			
			Client.context().log.i("Loading " + uri.toString() + "...");
			// Convert the stream into a string.
			if(status.getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				String content_string;
				byte[] buffer = new byte[512];
				int readBytes;
				
				loading: while ((readBytes = stream.read(buffer)) != -1) {
					//Client.context().log.i(new String(buffer));
					if(Thread.interrupted()) {
						entity.consumeContent();
						throw new InterruptedException("Interrupted loading of " + uri.toString());
					}
					
					content.write(buffer, 0, readBytes);
					content_string = new String(content.toByteArray());
					for(int i = 0 ; i < patterns.length ; i ++) {
						if(patterns[i].matches(content_string)){
							Client.context().log.i("Terminating " + uri.toString() + " due to pattern " + terminates[i].toString());
							entity.consumeContent();
							break loading;
						}
					}
				}
				content_string = content.toString();
				//cache.put(web_page, string_content);
				return content_string;
			} else {
				throw new BrowserException(uri.toString() + "returned error status: " + Integer.toString(status.getStatusCode()));
			}
		} catch(URISyntaxException e) {
			throw new BrowserException(url + " is not a well-formed URL.");
		} catch(SocketTimeoutException e) {
			throw new BrowserException("Timeout waiting for " + url + " to respond.");
		} catch(IOException e) {
			throw new BrowserException("Error " + e.toString() + " loading " + url);
		}
	}
	
	private static void addHeaders(HttpRequestBase http_request, AbstractResource[] headers, AbstractResult caller)
				throws ResourceNotFoundException, TemplateException, MissingVariable, BrowserException, InterruptedException {
		for(int i = 0 ; i < headers.length ; i ++) {
			Result header = headers[i].getValue(caller)[0];
			http_request.addHeader(header.key, header.value);
		}
	}
	
	/**
	 * Private method, converts an array of AbstractHeaders to an UrlEncodedFormEntity.
	 * @param headers.
	 * @throws UnsupportedEncodingException If the Map contains a string that cannot be encoded.
	 * @throws BrowserException 
	 * @throws InterruptedException 
	 * @throws MissingVariable 
	 * @throws TemplateException 
	 * @throws ResourceNotFoundException 
	 */
	private static UrlEncodedFormEntity generateFormEntity(AbstractResource[] headers, AbstractResult caller)
				throws UnsupportedEncodingException, ResourceNotFoundException, TemplateException, MissingVariable, BrowserException, InterruptedException {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		for(int i = 0; i < headers.length ; i ++) {
			Result header = headers[i].getValue(caller)[0];
			pairs.add(new BasicNameValuePair(header.key, header.value));
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
