package net.microscraper.client.interfaces;

/**
 * URI Interface because {@link java.net.uri} is not available in J2ME.
 * @author john
 *
 */
public interface URIInterface {
	public abstract boolean isAbsolute();

	public abstract URIInterface resolve(String link);
	public abstract URIInterface resolve(URIInterface otherURI);
	
	public abstract URIInterface resolveJSONFragment(String key) throws NetInterfaceException;
	public abstract URIInterface resolveJSONFragment(int index) throws NetInterfaceException;
	
	public abstract String getScheme();
	public abstract String getSchemeSpecificPart();
	public abstract String getFragment();
	
	public abstract String toString();
}
