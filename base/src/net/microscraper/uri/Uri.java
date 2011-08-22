package net.microscraper.uri;

import java.io.IOException;

/**
 * A {@link Uri} provides an interface for a URI.
 * @author realest
 *
 */
public interface Uri {
	/**
	 * Constructs a new {@link Uri} by resolving the given {@link Uri} against
	 * this {@link Uri}
	 * @param uri The {@link Uri} to resolve.
	 * @return The resulting {@link Uri}.
	 * @throws RemoteToLocalSchemeResolutionException if the returned {@link Uri} would have
	 * a local scheme when this one does not.
	 */
	public Uri resolve(Uri uri) throws RemoteToLocalSchemeResolutionException;
	
	/**
	 * Constructs a new {@link Uri}
	 * by parsing the given string as a URI and then resolving it against this
	 * {@link Uri}. 
	 * @param uri A {@link String} to parse as a URI.
	 * @return The resulting {@link Uri}.
	 * @throws MalformedUriException if <code>uri</code> is not a valid
	 * {@link Uri} and therefore cannot be resolved.
	 * @throws RemoteToLocalSchemeResolutionException if the returned {@link Uri} would have
	 * a local scheme when this one does not.
	 */
	public Uri resolve(String uri) throws MalformedUriException, RemoteToLocalSchemeResolutionException;
	
	/**
	 * Load the resource this {@link Uri} refers to as a {@link String}.
	 * @return The resource as a {@link String}.
	 * @throws IOException If there was a problem loading the resource.
	 * @throws InterruptedException If the user interrupts the load.
	 */
	public String load() throws IOException, InterruptedException;
	
	/**
	 * 
	 * @return Returns the content of this {@link Uri} as a string. 
	 * @see java.net.URI#toString()
	 */
	public String toString();
	
	/**
	 * Tests this {@link Uri} for equality with another object. 
	 * @param obj The object to compare.
	 * @return <code>True</code> if both objects are {@link Uri}s 
	 * referring to the same location, <code>false</code> otherwise.
	 * @see java.net.URI#equals(Object)
	 */
	public boolean equals(Object obj);

	/**
	 * 
	 * @return The scheme specific part of the URI.
	 */
	public String getSchemeSpecificPart();
}
