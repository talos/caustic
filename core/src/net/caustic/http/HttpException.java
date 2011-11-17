package net.caustic.http;

/**
 * Parent {@link Exception} class for both problems both making HTTP
 * requests and reading HTTP responses.
 * @author realest
 *
 */
public class HttpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8080881196440155065L;
	
	protected HttpException(String message) {
		super(message);
	}
}
