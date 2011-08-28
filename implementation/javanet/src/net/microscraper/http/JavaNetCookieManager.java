package net.microscraper.http;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.microscraper.http.BadURLException;
import net.microscraper.http.CookieManager;
import net.microscraper.http.ResponseHeaders;

public class JavaNetCookieManager implements CookieManager {

	private final java.net.CookieManager cookieManager = new java.net.CookieManager();
	
	private String[] getCookiesFor(String urlString, Hashtable requestHeaders, String cookieType) 
			throws BadURLException, IOException {
		try {
			Map<String, List<String>> requestHeaderMap = new HashMap<String, List<String>>();
			Enumeration<String> e = requestHeaders.keys();
			while(e.hasMoreElements()) {
				String name = e.nextElement();
				requestHeaderMap.put(name, Arrays.asList((String) requestHeaders.get(name)));
			}
			Map<String, List<String>> allCookies = cookieManager.get(new URI(urlString), requestHeaderMap);
			if(allCookies.containsKey(cookieType)) {
				List<String> cookies = allCookies.get(cookieType);
				return cookies.toArray( new String[cookies.size()] );
			} else {
				return new String[] { };
			}
		} catch(URISyntaxException e) {
			throw new BadURLException(e);
		}
	}
	
	@Override
	public String[] getCookiesFor(String urlString, Hashtable requestHeaders) throws BadURLException, IOException {
		return getCookiesFor(urlString, requestHeaders, COOKIE_HEADER_NAME);
	}
	
	@Override
	public String[] getCookie2sFor(String urlString, Hashtable requestHeaders) throws BadURLException, IOException {
		return getCookiesFor(urlString, requestHeaders, COOKIE_2_HEADER_NAME);
	}

	@Override
	public void addCookiesFromResponseHeaders(String url,
			ResponseHeaders responseHeaders) throws BadURLException, IOException {
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
			throw new BadURLException(e);
		}
	}

	@Override
	public void addCookies(String url, Hashtable cookies) throws BadURLException {
		CookieStore cookieStore = cookieManager.getCookieStore();
		Enumeration<String> names = cookies.keys();
		try {
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				String value = (String) cookies.get(name);
				cookieStore.add(new URI(url), new HttpCookie(name, value));
			}
		} catch(URISyntaxException e) {
			throw new BadURLException(e);
		}
	}

}
