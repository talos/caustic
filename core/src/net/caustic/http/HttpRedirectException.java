package net.caustic.http;

import net.caustic.util.StringUtils;

/**
 * This {@link HttpRequestException} should be thrown when there is a problem
 * following HTTP redirects.
 * @author realest
 *
 */
class HttpRedirectException extends HttpRequestException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1339632189785386222L;

	private HttpRedirectException(String message) {
		super(message);
	}
	
	/**
	 * Construct a {@link HttpRedirectException} because of a circular redirect.
	 * @param urls An array of {@link String}s of the URLs followed.
	 * @return A {@link HttpRedirectException}
	 */
	public static HttpRedirectException newCircular(String[] urls) {
		return new HttpRedirectException("Circular redirect found in " + StringUtils.quoteJoin(urls, ", "));
	}

	/**
	 * Construct a {@link HttpRedirectException} because the max number of redirects was hit.
	 * @param urls An array of {@link String}s of the URLs followed.
	 * @param numRedirects the number of redirects followed so far.
	 * @return A {@link HttpRedirectException}
	 */
	public static HttpRedirectException newMaxRedirects(String[] urls, int numRedirects) {
		return new HttpRedirectException("Hit maximum number of redirects " + numRedirects +
				" following " + StringUtils.quoteJoin(urls, ", "));
	}
	
	/**
	 * Construct a {@link HttpRedirectException} based off of a circular redirect.
	 * @param urls An array of {@link String}s of the URLs followed.
	 * @return A {@link HttpRedirectException}
	 */
	public static HttpRedirectException fromBadURL(BadURLException e) {
		return new HttpRedirectException("Bad URL prevented redirect: " + e.getMessage());
	}
}
