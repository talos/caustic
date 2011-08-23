package net.microscraper.file;

import java.io.IOException;

/**
 * Implementations of this interface can load the contents of local files
 * located at a URI.
 * @author realest
 *
 */
public interface FileLoader {

	/**
	 * Load the {@link String} contents from a file at a local URI.
	 * @param <code>uri</code> The {@link String} of the local URI to load from.
	 * @return A {@link String} with the contents of the file at
	 * the local <code>uri</code>.
	 * @throws IOException If the contents from the file at <code>uri</code>
	 * could not be loaded.
	 */
	public String load(String uri) throws IOException;
}
