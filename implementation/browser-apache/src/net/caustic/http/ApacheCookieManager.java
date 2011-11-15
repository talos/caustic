package net.caustic.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import net.caustic.http.BadURLException;
import net.caustic.http.CookieManager;
import net.caustic.http.ResponseHeaders;

public class ApacheCookieManager implements CookieManager {

	private final CookieStore cookieStore = new BasicCookieStore();

	@Override
	public void addCookies(String urlStr, Hashtable cookies)
			throws BadURLException {
		Iterator<String> iterator = cookies.keySet().iterator();
		while(iterator.hasNext()) {
			String name = iterator.next();
			String value = (String) cookies.get(name);
			addCookie(urlStr, name, value);
		}
	}

	@Override
	public CookieManager copy() {
		ApacheCookieManager copy = new ApacheCookieManager();
		for(Cookie cookie : cookieStore.getCookies()) {
			copy.addCookie(cookie.getDomain(), cookie.getName(), cookie.getValue());
		}
		return copy;
	}

	@Override
	public String[] getCookiesFor(String urlString, Hashtable requestHeaders)
			throws BadURLException {
		//List<Cookie> cookies = cookieStore.getCookies();
		List<String> cookies = new ArrayList<String>();
		
		for(Cookie cookie : cookieStore.getCookies()) {
			if(cookie.getDomain().equals(urlString)) {
				cookies.add(cookie.toString());
			}
		}
		
		return null;
	}

	@Override
	public String[] getCookie2sFor(String urlString, Hashtable requestHeaders)
			throws BadURLException {
		return getCookiesFor(urlString, requestHeaders);
	}

	@Override
	public void addCookiesFromResponseHeaders(String urlStr,
			ResponseHeaders responseHeaders) throws BadURLException,
			CookieStorageException {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		String[] headerNames = responseHeaders.getHeaderNames();
		for(int i = 0 ; i < headerNames.length ; i ++) {
			String headerName = headerNames[i];
			String[] headerValues = responseHeaders.getHeaderValues(headerName);
			headerMap.put(headerName, Arrays.asList(headerValues));
		}
		try {
			cookieStore.addCookie(new BasicClientCookie(name, value))
			cookieManager.put(new URI(url), headerMap);
		} catch(URISyntaxException e) {
			throw new BadURLException(url, e.getMessage());
		} catch(IOException e) {
			throw new CookieStorageException(e.getMessage());
		}
	}

	public void addCookiesFromResponseHeaders(String url,
			ResponseHeaders responseHeaders) throws BadURLException, CookieStorageException {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		String[] headerNames = responseHeaders.getHeaderNames();
		for(int i = 0 ; i < headerNames.length ; i ++) {
			String headerName = headerNames[i];
			String[] headerValues = responseHeaders.getHeaderValues(headerName);
			headerMap.put(headerName, Arrays.asList(headerValues));
		}
		try {
			cookieManager.put(new URI(url), headerMap);
		} catch(URISyntaxException e) {
			throw new BadURLException(url, e.getMessage());
		} catch(IOException e) {
			throw new CookieStorageException(e.getMessage());
		}
	}

	
	private void addCookie(String host, String name, String value) throws BadURLException {
		try {
			URL url = new URL(host);
			BasicClientCookie cookie = new BasicClientCookie(name, value);
			cookie.setDomain(url.getHost());
			cookie.setPath(url.getPath());
			cookieStore.addCookie(cookie);
		} catch(MalformedURLException e) {
			throw new BadURLException(host, e.getMessage());
		}
	}
}
