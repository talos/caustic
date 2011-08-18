package net.microscraper.uri;

/**
 * Subclasses instantiate {@link URIInterface}.
 * @author realest
 *
 */
public interface UriFactory {
	
	/**
	 * Generate a {@link Uri} from a {@link String} <code>uri</code>.
	 * @param uri The {@link String} to use.
	 * @return A {@link URIInterface}.
	 * @throws MalformedUriException If <code>uri</code> could not be used to create
	 * a {@link Uri}.
	 */
	public abstract Uri fromString(String uri) throws MalformedUriException;
}
