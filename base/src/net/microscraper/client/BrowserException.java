package net.microscraper.client;


/**
 * This is thrown when the {@link Browser} experiences an {@link Exception}.
 * @author john
 *
 */
public class BrowserException extends MicroscraperException {
	private final String url;
	/*protected BrowserException(String url) {
		this.url = url;
	}*/
	public BrowserException(String url, Throwable e) {
		super("Could not load " + url + ": " + e.getMessage(), e);
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