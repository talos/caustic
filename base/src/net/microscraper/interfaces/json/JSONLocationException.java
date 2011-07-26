package net.microscraper.interfaces.json;

import net.microscraper.ClientException;

/**
 * {@link JSONLocationException} is thrown when something has gone wrong with
 * a {@link JSONLocation}.
 * @see JSONLocation
 * @see ClientException
 * @author realest
 *
 */
public class JSONLocationException extends ClientException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7340201757487228364L;
	public JSONLocationException(String message ) {super(message); }
	public JSONLocationException(Throwable e ) {super(e); }
}
