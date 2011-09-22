package net.microscraper.http;

import java.util.Hashtable;

import net.microscraper.util.Encoder;

/**
 * The {@link CookieManager} stores and disburses cookies.
 * @author talos
 *
 */
public interface CookieManager {
	/**
	 * The Cookie request header.
	 */
	public static final String COOKIE_HEADER_NAME = "Cookie";
	
	/**
	 * The Cookie2 request header.
	 */
	public static final String COOKIE_2_HEADER_NAME = "Cookie2";

	/**
	 * Retrieve the cookies applicable to a request for <code>urlString</code>
	 * with <code>requestHeaders</code>.
	 * @param urlString The {@link String} URL of the request to get cookies for.
	 * @param requestHeaders The other headers used in the request, as a {@link String}
	 * to {@link String} map of name-values.
	 * @return An array of {@link String}s, each a single complete Cookie usable for the request.
	 * Is a zero-length array if there are none.
	 * @throws BadURLException If <code>urlString</code> can't be parsed as a URL.
	 */
	public String[] getCookiesFor(String urlString, Hashtable requestHeaders)
			throws BadURLException;
	
	/**
	 * Retrieve the cookie2s applicable to a request for <code>urlString</code>
	 * with <code>requestHeaders</code>.
	 * @param urlString The {@link String} URL of the request to get cookies for.
	 * @param requestHeaders The other headers used in the request, as a {@link String}
	 * to {@link String} map of name-values.
	 * @return An array of {@link String}s, each a single complete Cookie2 usable for the request.
	 * Is a zero-length array if there are none.
	 * @throws BadURLException If <code>urlString</code> can't be parsed as a URL.
	 */
	public String[] getCookie2sFor(String urlString, Hashtable requestHeaders)
			throws BadURLException;
	
	/**
	 * Add cookies from {@link ResponseHeaders} to the {@link CookieManager}.
	 * @param urlStr The <code>urlString</code> from which {@link ResponseHeaders} originated.
	 * @param responseHeaders The {@link ResponseHeaders} that may contain cookies.
	 * @throws BadURLException If <code>urlString</code> cannot be parsed as a URL.
	 * @throws CookieStorageException If cookies cannot be added, even though <code>
	 * urlString</code> is valid.
	 */
	public void addCookiesFromResponseHeaders(String urlStr,
			ResponseHeaders responseHeaders) throws BadURLException, CookieStorageException;

	/**
	 * Add cookies to the {@link CookieManager}.
	 * @param urlStr The {@link String} URL to use for the domain and path of the added cookies.
	 * @param cookies A {@link Hashtable} mapping {@link String} to {@link String} to use
	 * as name-value pairs for the cookies.  The values will be encoded by <code>encoder</code>
	 * @throws BadURLException If the <code>urlStr</code> can't be parsed as a URL.
	 */
	public void addCookies(String urlStr, Hashtable cookies) throws BadURLException;
	
	/**
	 * 
	 * @return A copy of this {@link CookieManager}, stocked with the same cookies.
	 */
	public CookieManager copy();
}
