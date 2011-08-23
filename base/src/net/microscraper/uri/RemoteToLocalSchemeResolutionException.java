package net.microscraper.uri;

/**
 * This should be thrown when the resolution of one {@link String} URI
 * against another using {@link UriResolver} crosses from a remote
 * scheme to a local one.
 * @author realest
 *
 */
public class RemoteToLocalSchemeResolutionException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7274455443453086198L;

	public RemoteToLocalSchemeResolutionException(String remoteUri, String resolveString) {
		super("Cannot resolve " + remoteUri + " with " + resolveString + " because this " +
				"would cross from remote to local scheme.");
	}
}
