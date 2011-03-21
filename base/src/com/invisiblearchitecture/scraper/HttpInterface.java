package com.invisiblearchitecture.scraper;

import java.io.IOException;
import java.util.Hashtable;

/**
 * A GeograpeContext is an interface that must be implemented by any code using the Geogrape library.  Its
 * implementations can be quite straightforward; for example, org.apache.httpcontext and java.util.regex
 * should be able to take care of these requirements.
 * @author john
 *
 */
public interface HttpInterface {

	/**
	 * This must be subclassed. Creates an object interfacing GeograpeEntity from 
	 * a url, a store of cookies (Gatherer-based), get values, post values, extra cookies, and headers.
	 * @param url A url as a String.
	 * @param cookieStore A GeograpeCookieStore, which is a wrapper for cookie access. Should be synchronized
	 * within individual Gatherers.
	 * @param gets A hashtable (k: String, v: String) of get variables.
	 * @param posts A hashtable (k: String, v: String) of post variables to set.
	 * @param cookies An array of cookies.  This array will be modified by response headers.
	 * @param headers A hashtable (k: String, v: String) of headers.
	 * @return
	 * @throws IOException
	 */
	public abstract EntityInterface attributesToEntity(String url, 
			CookieStoreInterface cookieStore, Hashtable gets, Hashtable posts,
			CookieInterface[] cookies, Hashtable headers) throws IOException;
	

	/**
	 * Provides an instance of CookieStoreInterface, used by Gatherers.
	 */
	public abstract CookieStoreInterface newCookieStore();
}
