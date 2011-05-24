package net.microscraper.server.resource;

import net.microscraper.client.interfaces.URIInterface;

public class URIMustBeAbsoluteException extends Exception {
	public URIMustBeAbsoluteException(URIInterface location) {
		super("URI '" + location.toString() + " should be absolute.");
	}
}
