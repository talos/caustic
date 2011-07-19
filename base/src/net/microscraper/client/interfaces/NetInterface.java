package net.microscraper.client.interfaces;

/**
 * Interface to obtain {@link URIInterface}.
 * @author realest
 *
 */
public interface NetInterface {
	public abstract URIInterface makeURI(String uriString) throws NetInterfaceException;
	public abstract URLInterface makeURL(String urlString) throws NetInterfaceException;
	public abstract URIInterface makeURI(String scheme, String schemeSpecificPart, String fragment)
				throws NetInterfaceException;
	public abstract Browser getBrowser();
	
}
