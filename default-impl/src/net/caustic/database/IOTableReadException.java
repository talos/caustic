package net.caustic.database;

/**
 * This is thrown when an {@link IOTable} cannot be read from.
 * @author realest
 *
 */
public class IOTableReadException extends DatabaseReadException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9163706707365174839L;

	public IOTableReadException(String message, Throwable e) {
		super(message, e);
	}
}
