package net.microscraper.resources.definitions;

import java.net.URI;

public interface Resource {
	/**
	 * 
	 * @return {@link java.net.URI} The resource's location.
	 */
	public abstract URI getLocation();
	
}
