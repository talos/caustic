package net.caustic.http;

/**
 * This {@link HttpException} is a superclass for all handled exceptions
 * that prevent an HTTP request from being completed (including redirects).
 * @author realest
 *
 */
class HttpRequestException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	protected HttpRequestException(String message) {
		super(message);
	}
	
}
