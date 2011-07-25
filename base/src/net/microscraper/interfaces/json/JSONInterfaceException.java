package net.microscraper.interfaces.json;

import net.microscraper.ClientException;

public class JSONInterfaceException extends ClientException {
	private static final long serialVersionUID = 1L;
	public JSONInterfaceException(String message ) {super(message); }
	public JSONInterfaceException(Throwable e ) {super(e); }
}