package net.microscraper.client.interfaces;

import java.net.URL;

/**
 * This is thrown when the Browser experiences an unrecoverable exception.
 * @author john
 *
 */
public class BrowserException extends Exception {
	private final URL url;
	public BrowserException(URL url, Throwable e) {
		super("Could not load " + url, e);
		this.url = url;
	}
	public URL getURL() {
		return this.url;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -849994574375042801L;
	
}