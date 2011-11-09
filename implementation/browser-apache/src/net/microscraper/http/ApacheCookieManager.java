package net.microscraper.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import net.microscraper.http.BadURLException;
import net.microscraper.http.CookieManager;
import net.microscraper.http.ResponseHeaders;

public class ApacheCookieManager implements CookieManager {

	private final CookieStore cookieStore = new BasicCookieStore();

	@Override
	public void addCookies(String urlStr, Hashtable cookies)
			throws BadURLException {
		try {
			URL url = new URL(urlStr);
			Iterator<String> iterator = cookies.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				String value = (String) cookies.get(name);
				BasicClientCookie cookie = new BasicClientCookie(name, value);
				cookie.setDomain(url.getHost());
				cookie.setPath(url.getPath());
				cookieStore.addCookie(cookie);
			}
		} catch(MalformedURLException e) {
			throw new BadURLException(urlStr, e.getMessage());
		}
	}

	@Override
	public CookieManager copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getCookiesFor(String urlString, Hashtable requestHeaders)
			throws BadURLException {
		// TODO Auto-generated method stub
		return null;
		
	}

	@Override
	public String[] getCookie2sFor(String urlString, Hashtable requestHeaders)
			throws BadURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookiesFromResponseHeaders(String urlStr,
			ResponseHeaders responseHeaders) throws BadURLException,
			CookieStorageException {
		// TODO Auto-generated method stub
		
	}

}