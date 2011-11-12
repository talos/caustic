package net.caustic.http;

/**
 * This {@link HttpRequestException} should be thrown when an HTTP request returns
 * a code that cannot be handled, for example 4XX.
 * @author realest
 *
 */
class BadHttpResponseCode extends HttpRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2317003148187125242L;
	
	/**
	 * 
	 * @param code The code that could not be handled.
	 */
	public BadHttpResponseCode(int code) {
		super("Cannot handle HTTP response code " + code);
	}
}
