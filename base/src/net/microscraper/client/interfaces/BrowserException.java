package net.microscraper.client.interfaces;

import net.microscraper.client.ClientException;

/**
 * This is thrown when the {@link Browser} experiences an {@link Exception}.
 * @author john
 *
 */
public class BrowserException extends NetInterfaceException {
	private final URLInterface url;
	protected BrowserException(URLInterface url) {
		this.url = url;
	}
	public BrowserException(URLInterface url, Throwable e) {
		super("Could not load " + url, e);
		this.url = url;
	}
	/**
	 * 
	 * @return The {@link URLInterface} that caused the {@link BrowserException}.
	 */
	public URLInterface getURL() {
		return this.url;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -849994574375042801L;
	
}