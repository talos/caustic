package net.microscraper.client.interfaces;

/**
 * This is a URI Interface, because {@link java.net.uri} is not available in J2ME.
 * Methods should work as they would in {@link java.net.uri}.
 * They also should include basic support for handling JSON fragments.
 * @author john
 *
 */
public interface URIInterface {
	
	/**
	 * Tells whether or not this URI is absolute.
	 * <p>A URI is absolute if, and only if, it has a scheme component.
	 * @return <code>true</code> if, and only if, this URI is absolute
	 */
	public abstract boolean isAbsolute();

	/**
	 * Constructs a new URI by parsing the given string and then resolving it against this URI.
	 * @param str - The string to be parsed into a URI
	 * @return The resulting URI
	 * @throws NullPointerException - If str is null
	 * @throws IllegalArgumentException - If the given string violates RFC 2396
	 */
	public abstract URIInterface resolve(String str) throws IllegalArgumentException;
	
	/**
	 * Resolves the given URI against this URI.
	 * If the given URI is already absolute, or if this URI is opaque, then the given URI is returned.
	 */
	public abstract URIInterface resolve(URIInterface otherURI);
	
	public abstract URIInterface resolveJSONFragment(String key) throws NetInterfaceException;
	public abstract URIInterface resolveJSONFragment(int index) throws NetInterfaceException;
	
	/**
	 * Returns the scheme component of this URI.
	 * <p>The scheme component of a URI, if defined, only contains characters in the alphanum category and in the string "-.+". A scheme always starts with an alpha character.
	 * <p>The scheme component of a URI cannot contain escaped octets, hence this method does not perform any decoding.
	 * @return The scheme component of this URI, or null if the scheme is undefined
	 */
	public abstract String getScheme();
	
	/**
	 * Returns the decoded scheme-specific part of this URI.
	 * <p>All sequences of escaped octets are decoded.
	 * @return The decoded scheme-specific part of this URI (never null)
	 */
	public abstract String getSchemeSpecificPart();
	
	/**
	 * Returns the decoded fragment component of this URI.
	 * <p>All sequences of escaped octets are decoded.
	 * @return The decoded fragment component of this URI, or null if the fragment is undefined
	 */
	public abstract String getFragment();
	
	/**
	 * Returns the content of this URI as a string.
	 * If this URI was created by invoking one of the constructors in this class then a string equivalent to the original input string, or to the string computed from the originally-given components, as appropriate, is returned. Otherwise this URI was created by normalization, resolution, or relativization, and so a string is constructed from this URI's components according to the rules specified in RFC 2396, section 5.2, step 7.
	 * @return The string form of this URI
	 */
	public abstract String toString();
}
