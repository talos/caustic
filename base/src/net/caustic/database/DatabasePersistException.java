package net.caustic.database;

/**
 * This {@link DatabaseException} is thrown when a {@link DatabaseView}
 * cannot be saved to.
 * @author realest
 *
 */
class DatabasePersistException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5749249468661471065L;
	
	public DatabasePersistException(String message) {
		super(message);
	}

	public DatabasePersistException(String message, Throwable e) {
		super(message, e);
	}
}
