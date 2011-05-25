package net.microscraper.client.interfaces;

/**
 * Interface to obtain {@link URIInterface}.
 * @author realest
 *
 */
public interface NetInterface {
	public abstract URIInterface getURI(String uriString) throws NetInterfaceException;
	public abstract URLInterface getURL(String urlString) throws NetInterfaceException;
}
