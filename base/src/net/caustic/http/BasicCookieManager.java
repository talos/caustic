package net.caustic.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.util.StringUtils;

/**
 * Idiotic cookie handling.  Stores only name-value pairs on a host-by-host basis.
 * @author talos
 *
 */
public class BasicCookieManager implements CookieManager {

	/**
	 * A {@link Hashtable} of domains mapped to {@link Hashtable}s of key-value pairs
	 * for the name and value of the cookie.
	 */
	private final Hashtable cookies = new Hashtable(); 
	
	public String[] getCookiesFor(String urlStr, Hashtable requestHeaders)
			throws BadURLException {

		Hashtable cookiesForDomain = getCookies(urlStr);
		final Vector result = new Vector();
		
		Enumeration e = cookiesForDomain.keys();
		while(e.hasMoreElements()) {
			String name = (String) e.nextElement();
			result.addElement(name + "=" + cookiesForDomain.get(name));
		}
		
		String[] resultAry = new String[result.size()];
		result.copyInto(resultAry);
		return resultAry;
	}

	public void addCookiesFromResponseHeaders(String urlStr,
			ResponseHeaders responseHeaders) throws BadURLException,
			CookieStorageException {
		Hashtable cookiesForDomain = getCookies(urlStr);
		
		String[] cookieHeaders = responseHeaders.getHeaderValues(SET_COOKIE_HEADER_NAME);
		if(cookieHeaders != null) {
			for(int i = 0 ; i < cookieHeaders.length ; i ++) {
				// name/value is first in cookie
				String[] nameValue = StringUtils.split(StringUtils.split(cookieHeaders[i], "; ")[0], "=");
				cookiesForDomain.put(nameValue[0], nameValue[1]);
			}
		}
	}

	public void addCookies(String urlStr, Hashtable cookies) throws BadURLException {
		Hashtable cookiesForDomain = getCookies(urlStr);
		
		Enumeration keys = cookies.keys();
		while(keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			cookiesForDomain.put(name, cookies.get(name));
		}
	}

	public CookieManager copy() {
		BasicCookieManager copy = new BasicCookieManager();
		
		// deep copy
		Enumeration hosts = cookies.keys();
		while(hosts.hasMoreElements()) {
			String host = (String) hosts.nextElement();
			Hashtable cookiesForHost = (Hashtable) cookies.get(host);
			Hashtable cookiesForHostCopy = new Hashtable();
			copy.cookies.put(host, cookiesForHostCopy);
			
			Enumeration names = cookiesForHost.keys();
			while(names.hasMoreElements()) {
				String name = (String) names.nextElement();
				cookiesForHostCopy.put(name, cookiesForHost.get(name));
			}
		}
		
		return copy;
	}
	
	/**
	 * Get the hash of name-value pairs for a host.  Creates a new hash
	 * if one doesn't exist yet for that host.
	 * @param urlStr A {String} path, from which the host will be isolated.
	 * @return
	 * @throws BadURLException if the host could not be extracted from <code>urlStr</code>.
	 */
	private Hashtable getCookies(String urlStr) throws BadURLException {
		try {
			String host = new URL(urlStr).getHost();
			Hashtable cookiesForHost= (Hashtable) cookies.get(host);
			
			if(cookiesForHost == null) {
				cookiesForHost = new Hashtable();
				cookies.put(host, cookiesForHost); // create hash for URL if it doens't already exist
			}
			return cookiesForHost;
		} catch(MalformedURLException e) {
			throw new BadURLException(urlStr, "Could not extract host");
		}
	}
}
