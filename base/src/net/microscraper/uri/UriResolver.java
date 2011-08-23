package net.microscraper.uri;

/**
 * Interface to resolve {@link String}s against one another as if they were
 * {@link URI}s.
 *
 */
public interface UriResolver {
	
	/**
	 * {@link #FILE_SCHEME} should allow resolution to a remote scheme.
	 */
	public static final String FILE_SCHEME = "file";
	
	/**
	 * Resolve a {@link String} against another as if both were {@link URI}s.
	 * @param uriStr The {@link String} URI to resolve against.
	 * @param resolveURIStr The {@link String} URI to resolve against <code>uri</code>.
	 * @return A {@link String} URI resulting from the resolution.
	 * @throws MalformedUriException If either argument is not a valid URI.
	 * @throws RemoteToLocalSchemeResolutionException If the resolution would
	 * involve going from a remote to a local scheme.
	 */
	public abstract String resolve(String uriStr, String resolveURIStr)
			throws MalformedUriException, RemoteToLocalSchemeResolutionException;
}