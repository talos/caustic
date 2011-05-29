package net.microscraper.client.interfaces;


/**
 * This is a URL Interface, because {@link java.net.url} is not available in J2ME.
 * Methods should work as they would in {@link java.net.url}.
 * @author john
 *
 */
public interface URLInterface {

	/**
	 * Gets the host name of this <code>URL</code>, if applicable.
	 * <p>The format of the host conforms to RFC 2732, i.e. for a literal IPv6 address, this method will return the IPv6 address enclosed in square brackets ('[' and ']').
	 * @return the host name of this <code>URL</code>.
	 */
	public abstract String getHost();
	
	/**
	 * Constructs a string representation of this <code>URL</code>. 
	 * @returna string representation of this object.
	 */
	public abstract String toString();
}
