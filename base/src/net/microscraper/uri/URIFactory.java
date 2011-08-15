package net.microscraper.uri;

/**
 * Subclasses instantiate {@link URIInterface}.
 * @author realest
 *
 */
public interface URIFactory {
	
	/**
	 * Generate a {@link URIInterface} from the {@link String} <code>uri</code>.
	 * @param uri The {@link String} to use.
	 * @return A {@link URIInterface}.
	 * @throws URIInterfaceException If <code>uri</code> could not be used to create
	 * a {@link URIInterface}.
	 */
	public abstract URIInterface fromString(String uri) throws URIInterfaceException;
}
