package net.microscraper.client.interfaces;

public class JSONInterfaceException extends Exception {
	private static final long serialVersionUID = 1L;
	public JSONInterfaceException(String message ) {super(message); }
	public JSONInterfaceException(Throwable e ) {super(e); }
}