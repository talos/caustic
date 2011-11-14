package net.caustic.database;

/**
 * This {@link Exception} class is thrown when there are problems
 * with a {@link Connection}.
 * @author realest
 *
 */
public class ConnectionException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -862687537285104773L;

	public ConnectionException(Throwable e) {
		super(e.getMessage());
	}
}
