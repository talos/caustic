package net.microscraper.file;

import java.io.IOException;

import net.microscraper.uri.URIInterface;

/**
 * Implementations of this interface can load the contents of files located at a
 * path.
 * @author realest
 *
 */
public interface FileLoader {

	/**
	 * Load a {@link String} from a path.
	 * @param path The path to load from.
	 * @return A String with the contents of the file at that the <code>path</code>.
	 * @throws IOException If the path could not be loaded.
	 */
	public String load(URIInterface path) throws IOException;
}
