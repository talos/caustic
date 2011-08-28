package net.microscraper.http;

import java.io.InputStreamReader;

/**
 * An interface for handling the response from a URL requested by
 * {@link HttpRequester}.
 * @author talos
 *
 */
public interface HttpResponse {

	public static final String LOCATION_HEADER_NAME = "location";
	public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
	public static final String CHARSET_KEY = "charset";
	
	/**
	 * Obtain the response's content.
	 * @return An {@link InputStreamReader} to read the content of the {@link HttpResponse}.
	 */
	public InputStreamReader getContentStream();

	/**
	 * 
	 * @return Whether the {@link HttpResponse} has content that can be pulled from
	 * {@link #getContentStream()}.
	 */
	public boolean isSuccess();
	
	/**
	 * @return The response's headers.
	 */
	public ResponseHeaders getResponseHeaders();
	
	/**
	 * 
	 * @return Whether this {@link HttpResponse} is a redirect.
	 */
	public boolean isRedirect();
	
	/**
	 * 
	 * @return The URL string that this response redirects to.
	 * @throws BadURLException 
	 */
	public String getRedirectLocation() throws BadURLException;
	
	/**
	 * 
	 * @return The {@link HttpResponse}'s response code.
	 */
	public int getResponseCode();
}
