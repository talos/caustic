package net.microscraper.server;

import net.microscraper.client.interfaces.URIInterface;

/**
 * {@link Resource}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public class Resource {
	/**
	 * 
	 * The resource's absolute {@link URIInterface} location.
	 */
	public final URIInterface location;
	
	/**
	 * {@link Resource} must be initialized with a {@link URIInterface} location.
	 * @param location The {@link URIInterface} where this {@link Resource} can be
	 * found.  Should be absolute.
	 * @throws InstantiationError If {@link #location} is not absolute.
	 */
	public Resource(URIInterface location) {
		if(location.isAbsolute()) {
			this.location = location;
		} else {
			throw new InstantiationError();
		}
	}
}
