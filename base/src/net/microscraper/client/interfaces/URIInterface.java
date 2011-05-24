package net.microscraper.client.interfaces;

/**
 * URI Interface because {@link java.net.uri} is not available in J2ME.
 * @author john
 *
 */
public interface URIInterface {
	public boolean isAbsolute();

	public URIInterface resolve(String link);
	public URIInterface resolve(URIInterface otherURI);
	
	public URIInterface resolveJSONFragment(String key);
	public URIInterface resolveJSONFragment(int index);
}
