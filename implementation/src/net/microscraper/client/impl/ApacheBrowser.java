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
import net.microscraper.client.impl.CookieInterface;
import net.microscraper.client.impl.CookieStoreInterface;
import net.microscraper.client.impl.deprecated.EntityInterface;
import net.microscraper.client.impl.deprecated.HttpInterface;
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

	@Override
	public String load(WebPage web_page) throws InterruptedException,
			BrowserException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Private method, converts a Map<String, String> to an UrlEncodedFormEntity.
	 * @param hm The Map to be encoded.
	 * @throws UnsupportedEncodingException If the Map contains a string that cannot be encoded.
	 */
	private UrlEncodedFormEntity toUrlEncodedFormEntity(Map<String, String> hm) throws UnsupportedEncodingException {
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
		if(hm == null) {
			return new UrlEncodedFormEntity(nvps); // Return an empty.
		}
		
		Iterator<String> iterator = hm.keySet().iterator();
		while(iterator.hasNext()) {
			String k = iterator.next();
			nvps.add(new BasicNameValuePair(k, hm.get(k)));
		}	
		
		UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(nvps);
		
		return uefe;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public EntityInterface attributesToEntity(String urlString,
			CookieStoreInterface gCookieStore, 
			Hashtable gets, Hashtable posts, CookieInterface[] gCookies,
			Hashtable headers) throws IOException {
		
		final URI url;
		try {
			url = new URI(urlString);
		} catch(URISyntaxException e) {
			throw new IOException(e.toString());
		}
		if(gets == null)
			gets = new Hashtable();
		if(posts == null)
			posts = new Hashtable();
		if(headers == null)
			headers = new Hashtable();
		if(gCookieStore == null)
			gCookieStore = newCookieStore();
		
		if(gCookies != null)
			gCookieStore.addCookies(gCookies);
				
		// Set up our httpclient to handle redirects.
		HttpParams httpParams = new BasicHttpParams();
		httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		httpParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		httpParams.setParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, false);
		httpParams.setParameter(ClientPNames.MAX_REDIRECTS, 100);
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.setParams(httpParams);
		
		httpClient.setRedirectHandler(new DefaultRedirectHandler() {
			
			// Force handling of 302s.  Default implementation does not handle them
			// with POST requests.
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
				
				return url.resolve(redirect);
			}
		});
		
		// Do we have a Post request or a Get request?
		String getArgs = EntityUtils.toString(toUrlEncodedFormEntity(gets));
		final HttpRequestBase httpRequest;
		if(posts.size() > 0) {
			httpRequest = new HttpPost(url.toString() + '?' + getArgs);
			
			((HttpEntityEnclosingRequestBase) httpRequest).setEntity(toUrlEncodedFormEntity(posts));
		} else {
			httpRequest = new HttpGet(url.toString() + '?' + getArgs);
		}
		
		Enumeration headerKeys = headers.keys();
		while(headerKeys.hasMoreElements()) {
			String key = (String) headerKeys.nextElement();
			String value = (String) headers.get(key);
			httpRequest.addHeader(key, value);
		}
		
		BasicCookieStore reqCookieStore = new BasicCookieStore();
		Cookie[] reqCookies = ApacheCookie.arrayToCookieArray(gCookieStore.getCookies());
		reqCookieStore.addCookies(reqCookies);
		
		httpClient.setCookieStore(reqCookieStore);
		
		HttpResponse response = httpClient.execute(httpRequest);
		
		// Add any additional cookies from the response back to the cookieStore interface.
		List<CookieInterface> responseCookies = new ArrayList<CookieInterface>();
		for(int i = 0; i < reqCookieStore.getCookies().size(); i++) {
			responseCookies.add(new ApacheCookie(reqCookieStore.getCookies().get(i)));
		}
		
		gCookieStore.addCookies(responseCookies.toArray(new CookieInterface[0]));
		
		// Deal with errors.  Does this follow redirects? I hope so.
		StatusLine status = response.getStatusLine();
		
		if(status.getStatusCode() == 200) {
			return new ApacheEntity(response.getEntity());
		} else {
			/*HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			byte[] buffer = new byte[512];
			while(stream.read(buffer) != -1) {
				System.out.println(new String(buffer));
			}*/
			throw new HttpResponseException(status.getStatusCode(), "Unable to get content: " + Integer.toString(status.getStatusCode()));
		}
	}

	@Override
	public CookieStoreInterface newCookieStore() {
		return new ApacheCookieStore();
	}
	
	/**
	 * Synchronized implementation of GeograpeCookieStore based off of Apache HTTP libraries.
	 * @author realest
	 *
	 */
	private static class ApacheCookieStore implements CookieStoreInterface {
		private final BasicCookieStore cookieStore = new BasicCookieStore();
		
		@Override
		public CookieInterface[] getCookies() {
			List<Cookie> cookies = cookieStore.getCookies();
			CookieInterface[] gCookies = new CookieInterface[cookies.size()];
			for(int i = 0; i < cookies.size(); i++) {
				gCookies[i] = new ApacheCookie(cookies.get(i));
			}
			return gCookies;
		}
		
		@Override
		public synchronized void addCookies(CookieInterface[] gCookies) {
			Cookie[] cookies = new Cookie[gCookies.length];
			for(int i = 0; i < gCookies.length; i++) {
				CookieInterface gCookie = gCookies[i];
				BasicClientCookie cookie = new BasicClientCookie(gCookie.getName(), gCookie.getValue());
				cookie.setPath(gCookie.getPath());
				cookie.setDomain(gCookie.getDomain());
				cookie.setExpiryDate(gCookie.getExpiryDate());
				cookies[i] = cookie;
			}
			cookieStore.addCookies(cookies);
		}
		
		@Override
		public String toString() {
			return cookieStore.toString();
		}
	}
	
	/**
	 * Implementation of GeograpeCookie based off Apache HTTP libraries.
	 * Initialized with a Cookie interface.
	 * @author realest
	 *
	 */
	private static class ApacheCookie implements CookieInterface {
		private final Cookie cookie;
		public ApacheCookie(Cookie c) {
			cookie = c;
		}
		@Override
		public String getDomain() { return cookie.getDomain(); }

		@Override
		public Date getExpiryDate() { return cookie.getExpiryDate(); }

		@Override
		public String getName() { return cookie.getName(); }

		@Override
		public String getPath() { return cookie.getPath(); }

		@Override
		public String getValue() { return cookie.getValue(); }
		
		@Override
		public String toString() {
			return cookie.toString();
		}
		
		/**
		 * Convert an ApacheCookie to a Cookie that can be added to a CookieStore.
		 * @return
		 */
		public static final Cookie toCookie(CookieInterface c) {
			BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
			cookie.setDomain(c.getDomain());
			cookie.setExpiryDate(c.getExpiryDate());
			cookie.setPath(c.getPath());
			return cookie;
		}
		
		/**
		 * Convert an array of ApacheCookies to a Cookie that can be added to a CookieStore
		 */
		public static final Cookie[] arrayToCookieArray(CookieInterface[] cArray) {
			Cookie[] cookies = new Cookie[cArray.length];
			for(int i = 0 ; i < cookies.length; i++) {
				cookies[i] = toCookie(cArray[i]);
			}
			return cookies;
		}
	}
	

	final class ApacheEntity implements EntityInterface {
		private HttpEntity entity;
		
		public ApacheEntity(HttpEntity e) {
			entity = e;
		}
		
		@Override
		public InputStream getInputStream() throws IllegalStateException, IOException {
			return entity.getContent();
		}

		@Override
		public void consumeContent() throws IOException {
			entity.consumeContent();
		}
		
	}

}
