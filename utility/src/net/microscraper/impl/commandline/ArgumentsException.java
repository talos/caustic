package net.microscraper.impl.commandline;

/**
 * Exception class for when there is a problem with the {@link Arguments}
 * passed by the user.
 * @author realest
 *
 */
public class ArgumentsException extends Exception {

	public ArgumentsException(String string) {
		super(string);
	}
	
}
