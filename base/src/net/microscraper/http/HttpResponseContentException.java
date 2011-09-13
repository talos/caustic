package net.microscraper.http;

import java.io.IOException;

/**
 * This {@link HttpException} is thrown when there is a problem obtaining
 * a the content of a response from an HTTP response.
 * @author realest
 *
 */
public class HttpResponseContentException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6838397884392905425L;

	protected HttpResponseContentException(String message) {
		super(message);
	}
	
	/**
	 * Oftentimes the content of a {@link HttpResponse} cannot be fully retrieved
	 * because of an {@link IOException}.  This wraps {@link IOException}.
	 * @param e An {@link IOException}.
	 * @return a {@link HttpResponseContentException}.
	 */
	public static HttpResponseContentException fromIOException(IOException e) {
		return new HttpResponseContentException(e.getMessage());
	}
}
