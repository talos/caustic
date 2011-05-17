package net.microscraper.resources.definitions;

import java.net.URI;

public abstract class Resource {
	/**
	 * 
	 * The resource's absolute {@link java.net.URI} location.
	 */
	public final URI location;
	
	protected Resource(URI location) {
		this.location = location;
	}
}
