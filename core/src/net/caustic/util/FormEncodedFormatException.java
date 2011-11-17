package net.caustic.util;

/**
 * This {@link Exception} is thrown when a {@link String} cannot be converted
 * to form-encoded format.
 * @author talos
 *
 */
public class FormEncodedFormatException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2647491655991329747L;

	public FormEncodedFormatException(String message) {
		super(message);
	}
}
