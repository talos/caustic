package net.microscraper.server;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

public class DeserializationException extends Exception {

	public DeserializationException(Throwable e, JSONInterfaceObject jsonObject) {
		super(e);
	}
	public DeserializationException(Throwable e, JSONInterfaceArray jsonArray, int index) {
		super(e);
	}
	public DeserializationException(String msg, JSONInterfaceObject jsonObject) {
		super(msg);
	}
}
