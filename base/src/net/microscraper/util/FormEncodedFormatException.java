package net.microscraper.util;

/**
 * This {@link Exception} is thrown when a {@link String} cannot be converted
 * to form-encoded format.
 * @author talos
 *
 */
public class FormEncodedFormatException extends Exception {
	public FormEncodedFormatException(String message) {
		super(message);
	}
}
