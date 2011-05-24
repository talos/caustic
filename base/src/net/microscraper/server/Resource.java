package net.microscraper.server;

import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.resource.URIMustBeAbsoluteException;

public class Resource {
	/**
	 * 
	 * The resource's absolute {@link URIInterface} location.
	 */
	public final URIInterface location;
	
	public Resource(URIInterface location) throws URIMustBeAbsoluteException {
		if(location.isAbsolute()) {
			this.location = location;
		} else {
			throw new URIMustBeAbsoluteException(location);
		}
	}
}
