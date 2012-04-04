package net.caustic.uri;

/**
 * This is thrown if a request is made to the URILoader for a scheme
 * that it does not support.
 * @author talos
 *
 */
public class UnsupportedSchemeException extends URILoaderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsupportedSchemeException(String scheme) {
		super("Scheme '" + scheme + "' is not supported.");
	}
}
