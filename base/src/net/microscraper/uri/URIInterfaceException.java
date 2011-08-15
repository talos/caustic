package net.microscraper.uri;

import net.microscraper.client.MicroscraperException;

/**
 * {@link URIInterfaceException} is thrown when something has gone wrong with
 * a {@link URIInterface}.
 * @see URIInterface
 * @see MicroscraperException
 * @author realest
 *
 */
public class URIInterfaceException extends MicroscraperException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7340201757487228364L;
	public URIInterfaceException(String message ) {super(message); }
	public URIInterfaceException(Throwable e ) {super(e); }
}
