package net.microscraper.console;

/**
 * Exception class for when there is a problem with an {@link Option}
 * passed by the user.
 * @author realest
 *
 */
public class InvalidOptionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4015601786550502332L;

	public InvalidOptionException(String string) {
		super(string);
	}
	
}
