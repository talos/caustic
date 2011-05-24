package net.microscraper.client.interfaces;

/**
 * This is thrown when the Browser experiences an unrecoverable exception.
 * @author john
 *
 */
public class BrowserException extends Exception {
	private final URLInterface url;
	public BrowserException(URLInterface url, Throwable e) {
		super("Could not load " + url, e);
		this.url = url;
	}
	public URLInterface getURL() {
		return this.url;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -849994574375042801L;
	
}