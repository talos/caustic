package net.microscraper.uri;

import java.io.IOException;

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
	 * @throws IOException If the contents from the file at <code>uriStr</code>
	 * could not be loaded.
	 */
	public String load(String uriStr) throws IOException;
}
