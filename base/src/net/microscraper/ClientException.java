package net.microscraper;

/**
 * Parent class of all {@link Exception}s thrown by microscraper.
 * @author john
 *
 */
public class ClientException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8899853760225376402L;

	public ClientException() { super(); }
	public ClientException(String message) { super(message); }
	public ClientException(Throwable cause) { super(cause); }
	public ClientException(String message, Throwable cause) { super(message, cause); }
}