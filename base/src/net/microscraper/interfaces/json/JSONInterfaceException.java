package net.microscraper.interfaces.json;

import net.microscraper.ClientException;

/**
 * {@link JSONInterfaceException} is thrown when something has gone wrong
 * with a {@link JSONInterface}.
 * @see ClientException
 * @see JSONInterface
 * @author realest
 *
 */
public class JSONInterfaceException extends ClientException {
	private static final long serialVersionUID = 1L;
	public JSONInterfaceException(String message ) {super(message); }
	public JSONInterfaceException(Throwable e ) {super(e); }
}