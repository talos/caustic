package net.microscraper.interfaces.browser;

import net.microscraper.ClientException;

/**
 * This is thrown when the {@link Browser} experiences an {@link Exception}.
 * @author john
 *
 */
public class BrowserException extends ClientException {
	private final String url;
	protected BrowserException(String url) {
		this.url = url;
	}
	public BrowserException(String url, Throwable e) {
		super("Could not load " + url, e);
		this.url = url;
	}
	/**
	 * 
	 * @return The URL that caused the {@link BrowserException}.
	 */
	public String getURL() {
		return this.url;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -849994574375042801L;
	
}