package net.microscraper.interfaces.uri;

import net.microscraper.ClientException;

/**
 * {@link URIInterfaceException} is thrown when something has gone wrong with
 * a {@link URIInterface}.
 * @see URIInterface
 * @see ClientException
 * @author realest
 *
 */
public class URIInterfaceException extends ClientException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7340201757487228364L;
	public URIInterfaceException(String message ) {super(message); }
	public URIInterfaceException(Throwable e ) {super(e); }
}
