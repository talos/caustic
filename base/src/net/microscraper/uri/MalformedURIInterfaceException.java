package net.microscraper.uri;

/**
 * {@link MalformedURIInterfaceException} is thrown when a {@link URIInterface}
 * could not be created.
 * @see URIInterface
 * @author realest
 *
 */
public class MalformedURIInterfaceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7340201757487228364L;
	public MalformedURIInterfaceException(String message ) {super(message); }
	public MalformedURIInterfaceException(Throwable e ) {super(e); }
}
