package net.microscraper.client.interfaces;

import java.io.IOException;

/**
 * Implementations of this interface can load the contents of files located at a {@link URIInterface}
 * into a {@link String}.
 * @author realest
 *
 */
public interface URILoader {
	
	/**
	 * Load a String from a {@link URIInterface}.
	 * @param link The {@link URIInterface} to load from.
	 * @return A String with the contents of the file at that {@link URIInterface}.
	 * @throws IOException If the {@link URIInterface} could not be loaded.
	 */
	public String load(URIInterface uri) throws IOException;

}
