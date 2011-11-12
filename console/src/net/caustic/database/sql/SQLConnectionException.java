package net.caustic.database.sql;

import net.caustic.database.ConnectionException;

/**
 * {@link Exception} class for exceptions arising from {@link SQLConnection}.
 * @author talos
 * @see SQLConnection
 *
 */
public final class SQLConnectionException extends ConnectionException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8852343677241001261L;

	//public SQLConnectionException(String message) { super(message); }
	public SQLConnectionException(Throwable e) {
		super(e);
	}
	//public SQLConnectionException(String message, Throwable e) { super(message, e); }
}