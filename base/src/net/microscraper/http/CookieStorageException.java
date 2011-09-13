package net.microscraper.http;

/**
 * This {@link Exception} is thrown when {@link CookieManager}
 * cannot store a cookie.
 * @author realest
 *
 */
public class CookieStorageException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -124484083215012353L;
	
	public CookieStorageException(String message) {
		super(message);
	}
}
