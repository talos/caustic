package net.microscraper.database;

/**
 * This {@link Exception} is thrown by {@link DatabaseViewListener}
 * implementations when a hook can't successfully fire.
 * @author talos
 *
 */
public class DatabaseViewHookException extends DatabaseException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8302023440923180983L;

	public DatabaseViewHookException(String message, Throwable e) {
		super(message, e);
	}
}
