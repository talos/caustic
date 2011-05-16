package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class DeserializationException extends Exception {

	public DeserializationException(JSONInterfaceException e) {
		super(e);
	}
	public DeserializationException(String msg) {
		super(msg);
	}
}
