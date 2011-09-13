package net.microscraper.file;

import java.io.IOException;

/**
 * Implementations of this interface can load the contents of local files
 * located at a path.
 * @author realest
 *
 */
public interface FileLoader {

	/**
	 * Load the {@link String} contents from a file at a local path.
	 * @param <code>path</code> The {@link String} of the local path to load from.
	 * @return A {@link String} with the contents of the file at
	 * the local <code>path</code>.
	 * @throws IOException If the contents from the file at <code>path</code>
	 * could not be loaded.
	 */
	public String load(String path) throws IOException;
}
