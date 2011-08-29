package net.microscraper.http;

import java.io.IOException;
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
	 * @throws IOException If there is some other problem with the {@link CookieManager}
	 */
	public String[] getCookiesFor(String urlString, Hashtable requestHeaders)
			throws BadURLException, IOException;
	
	/**
	 * Retrieve the cookie2s applicable to a request for <code>urlString</code>
	 * with <code>requestHeaders</code>.
	 * @param urlString The {@link String} URL of the request to get cookies for.
	 * @param requestHeaders The other headers used in the request, as a {@link String}
	 * to {@link String} map of name-values.
	 * @return An array of {@link String}s, each a single complete Cookie2 usable for the request.
	 * Is a zero-length array if there are none.
	 * @throws BadURLException If <code>urlString</code> can't be parsed as a URL.
	 * @throws IOException If there is some other problem with the {@link CookieManager}
	 */
	public String[] getCookie2sFor(String urlString, Hashtable requestHeaders)
			throws BadURLException, IOException;
	
	/**
	 * Add cookies from {@link ResponseHeaders} to the {@link CookieManager}.
	 * @param urlString The <code>urlString</code> from which {@link ResponseHeaders} originated.
	 * @param responseHeaders The {@link ResponseHeaders} that may contain cookies.
	 * @throws BadURLException If <code>urlString</code> cannot be parsed as a URL.
	 * @throws IOException If there is some other problem with the {@link CookieManager}
	 */
	public void addCookiesFromResponseHeaders(String urlString,
			ResponseHeaders responseHeaders) throws BadURLException, IOException;

	/**
	 * Add cookies to the {@link CookieManager}.
	 * @param urlStr The {@link String} URL to use for the domain and path of the added cookies.
	 * @param cookies A {@link Hashtable} mapping {@link String} to {@link String} to use
	 * as name-value pairs for the cookies.  The values will be encoded by <code>encoder</code>
	 * @param encoder The {@link Encoder} to use when encoding cookie names and values.
	 * @throws BadURLException If the <code>urlStr</code> can't be parsed as a URL.
	 */
	public void addCookies(String urlStr, Hashtable cookies, Encoder encoder) throws BadURLException;
}
