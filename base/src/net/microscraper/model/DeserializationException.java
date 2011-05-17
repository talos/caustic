package net.microscraper.model;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class DeserializationException extends Exception {

	public DeserializationException(JSONInterfaceException e, Interfaces.JSON.Object jsonObject) {
		super(e);
	}
	public DeserializationException(JSONInterfaceException e, Interfaces.JSON.Array jsonArray, int index) {
		super(e);
	}
	public DeserializationException(URIMustBeAbsoluteException e, Interfaces.JSON.Object jsonObject) {
		super(e);
	}
	public DeserializationException(String msg, Interfaces.JSON.Object jsonObject) {
		super(msg);
	}
}
