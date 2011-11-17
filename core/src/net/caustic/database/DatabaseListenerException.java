package net.caustic.database;

/**
 * This {@link Exception} is thrown by {@link DatabaseListener}
 * implementations when a hook can't successfully fire.
 * @author talos
 *
 */
public class DatabaseListenerException extends DatabaseException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8302023440923180983L;

	public DatabaseListenerException(String message, Throwable e) {
		super(message, e);
	}
}
