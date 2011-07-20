package net.microscraper.interfaces.file;

import java.io.IOException;

/**
 * Implementations of this interface can load the contents of files located at a URI
 * or a path into a {@link String}.
 * @author realest
 *
 */
public interface FileLoader {
	
	/**
	 * Load a String from a URI.
	 * @param link The URI to load from.
	 * @return A String with the contents of the file at that {@link URIInterface}.
	 * @throws IOException If the URI could not be loaded.
	 */
	public String loadURI(String uri) throws IOException;

	/**
	 * Load a String from a path.
	 * @param path The path to load from.
	 * @return A String with the contents of the file at that {@link URIInterface}.
	 * @throws IOException If the path could not be loaded.
	 */
	public String loadPath(String path) throws IOException;

}
