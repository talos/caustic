package net.microscraper.http;

import net.microscraper.util.StringUtils;

/**
 * This {@link Exception} is thrown when a {@link String} is not a valid
 * URL.
 * @author talos
 *
 */
public class BadURLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8615649313297986L;

	/**
	 * Construct a new {@link BadURLException}.
	 * @param url The {@link String} URL that could not be parsed.
	 * @param message A {@link String} explanation as to why.
	 */
	public BadURLException(String url, String message) {
		super(StringUtils.quote(url) + " could not be parsed as a URL: " + message);
	}
	
}
