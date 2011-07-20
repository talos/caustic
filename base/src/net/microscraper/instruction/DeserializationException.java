package net.microscraper.instruction;

import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;

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
