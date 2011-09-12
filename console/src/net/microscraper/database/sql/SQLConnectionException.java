package net.microscraper.database.sql;

/**
 * {@link Exception} class for exceptions arising from {@link SQLConnection}.
 * @author talos
 * @see SQLConnection
 *
 */
public final class SQLConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	public SQLConnectionException(String message) { super(message); }
	public SQLConnectionException(Throwable e) { super(e); }
	public SQLConnectionException(String message, Throwable e) { super(message, e); }
}