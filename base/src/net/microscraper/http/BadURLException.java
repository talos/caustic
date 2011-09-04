package net.microscraper.http;

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

	public BadURLException(Throwable e) {
		super(e);
	}
	
}
