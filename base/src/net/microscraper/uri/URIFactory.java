package net.microscraper.uri;

/**
 * Subclasses instantiate {@link Uri}.
 * @author realest
 *
 */
public interface UriFactory {
	
	/**
	 * Generate a {@link Uri} from the {@link String} <code>uri</code>.
	 * @param uri The {@link String} to use.
	 * @return A {@link Uri}.
	 * @throws MalformedUriException If <code>uri</code> could not be used to create
	 * a {@link Uri}.
	 */
	public abstract Uri fromString(String uri) throws MalformedUriException;
}
