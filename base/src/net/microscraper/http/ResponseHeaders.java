package net.microscraper.http;

/**
 * Class to handle response headers from an {@link HttpResponse}.
 * @author talos
 *
 */
public interface ResponseHeaders {
	
	/**
	 * 
	 * @return An array of {@link String}s containing an element
	 * for the name of every header in {@link ResponseHeaders}.
	 * A zero-length list if there are no response headers.
	 */
	public String[] getHeaderNames();
	
	/**
	 * 
	 * @param headerName The header name whose values should be
	 * returned.
	 * @return An array of {@link String}s containing an element
	 * for every value with the name <code>headerName</code>.
	 * Returns <code>null</code> if {@link ResponseHeaders} has
	 * no headers under <code>headerName</code>.
	 */
	public String[] getHeaderValues(String headerName);
}
