package net.microscraper.http;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.microscraper.http.BadURLException;
import net.microscraper.http.CookieManager;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.util.Encoder;

public class JavaNetCookieManager implements CookieManager {

	private final java.net.CookieManager cookieManager = new java.net.CookieManager();
	//private final java.net.CookieStore cookieStore = new java.net.CookieManager().getCookieStore();
	
	private String[] getCookiesFor(String urlString, Hashtable requestHeaders, String cookieType) 
			throws BadURLException, IOException {
		try {
			CookieStore cookieStore = cookieManager.getCookieStore();
			List<HttpCookie> cookies = cookieStore.get(new URI(urlString));
			List<String> cookieStrs = new ArrayList<String>();
			
			Iterator<HttpCookie> iter = cookies.iterator();
			while(iter.hasNext()) {
				HttpCookie cookie = iter.next();
				cookieStrs.add(cookie.toString());
			}
			return cookieStrs.toArray(new String[cookieStrs.size()]);
			//System.out.println("Cookie Store: " +cookieManager.getCookieStore().get(new URI(urlString)));

			/*Map<String, List<String>> requestHeaderMap = new HashMap<String, List<String>>();
			Enumeration<String> e = requestHeaders.keys();
			while(e.hasMoreElements()) {
				String name = e.nextElement();
				requestHeaderMap.put(name, Arrays.asList((String) requestHeaders.get(name)));
			}*/
			//Map<String, List<String>> allCookies = cookieManager.get(new URI(urlString), requestHeaderMap);
			
			//System.out.println("Cookie Store: " +cookieManager.getCookieStore().get(new URI(urlString)));
			
			
			/*
			if(allCookies.containsKey(cookieType)) {
				List<String> cookies = allCookies.get(cookieType);
				
				System.out.println(cookieType + ": " + cookies);
				return cookies.toArray( new String[cookies.size()] );
			} else {
				return new String[] { };
			}
			*/
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
		//return getCookiesFor(urlString, requestHeaders, COOKIE_2_HEADER_NAME);
		return new String[] {};
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
	public void addCookies(String url, Hashtable cookies, Encoder encoder) throws BadURLException {
		CookieStore cookieStore = cookieManager.getCookieStore();
		Enumeration<String> names = cookies.keys();
		try {
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				String value = (String) cookies.get(name);
				HttpCookie cookie = new HttpCookie(encoder.encode(name), encoder.encode(value));
				cookie.setVersion(0); // Otherwise there are quotes around the value. See http://stackoverflow.com/questions/572482/simple-java-cookie-question/7225772#7225772
				cookieStore.add(new URI(url), cookie);
			}
		} catch(URISyntaxException e) {
			throw new BadURLException(e);
		}
	}

}
