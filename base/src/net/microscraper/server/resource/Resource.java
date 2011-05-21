package net.microscraper.server.resource;

import java.net.URI;

public abstract class Resource {
	/**
	 * 
	 * The resource's absolute {@link java.net.URI} location.
	 */
	public final URI location;
	
	protected Resource(URI location) throws URIMustBeAbsoluteException {
		if(location.isAbsolute()) {
			this.location = location;
		} else {
			throw new URIMustBeAbsoluteException(location);
		}
	}
}
