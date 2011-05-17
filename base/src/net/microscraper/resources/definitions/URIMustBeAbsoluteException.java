package net.microscraper.resources.definitions;

import java.net.URI;

public class URIMustBeAbsoluteException extends Exception {
	public URIMustBeAbsoluteException(URI uri) {
		super("URI '" + uri.toString() + " should be absolute.");
	}
}
