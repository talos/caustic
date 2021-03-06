package net.caustic.uri;

/**
 * {@link MalformedUriException} is thrown when a {@link Uri}
 * could not be created.
 * @see Uri
 * @author realest
 *
 */
public class MalformedUriException extends URILoaderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7340201757487228364L;
	public MalformedUriException(String message ) {super(message); }
	public MalformedUriException(Throwable e ) { super(e.getMessage()); }
}
