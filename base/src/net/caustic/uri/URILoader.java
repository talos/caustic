package net.caustic.uri;

/**
 * Interface to load the {@link String} contents of a URI, either local or remote.
 * @author talos
 *
 */
public interface URILoader {

	/**
	 * Load the {@link String} contents from a file at a local or remote URI.
	 * @param uriStr The {@link String} of the URI to load from.
	 * @return A {@link String} with the contents of the file at
	 * the <code>uriStr</code>.
	 * @throws URILoaderException if the file at <code>uriStr</code> cannot
	 * be loaded.
	 * @throws InterruptedException if the user interrupted while loading the file
	 * at <code>uriStr</code>.
	 */
	public String load(String uriStr) throws URILoaderException, InterruptedException;
}
