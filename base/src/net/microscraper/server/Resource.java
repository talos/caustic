package net.microscraper.server;

import java.net.URI;

import net.microscraper.server.resource.URIMustBeAbsoluteException;

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
