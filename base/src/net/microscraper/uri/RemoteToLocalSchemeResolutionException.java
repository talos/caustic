package net.microscraper.uri;

/**
 * This should be thrown when the resolution of one {@link Uri}
 * against another crosses from a remote scheme to a local one.
 * @author realest
 *
 */
public class RemoteToLocalSchemeResolutionException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7274455443453086198L;

	public RemoteToLocalSchemeResolutionException(Uri remoteUri, String resolveString) {
		super("Cannot resolve " + remoteUri + " with " + resolveString + " because it " +
				"crosses from remote to local scheme.");
	}
}
