package net.microscraper.client.interfaces;

import net.microscraper.client.ClientException;

/**
 * Generic {@link Exception} class for problems with a {@link NetInterface}.
 * @author john
 *
 */
public class NetInterfaceException extends ClientException {

	public NetInterfaceException() { };
	public NetInterfaceException(Throwable e) {
		super(e);
	}
	public NetInterfaceException(String message,Throwable cause) { super(message ,cause); }

}
