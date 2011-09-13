package net.microscraper.database;

import java.io.IOException;

/**
 * This {@link DatabaseException} is thrown when a {@link DatabaseView}
 * cannot be saved to.
 * @author realest
 *
 */
public class DatabasePersistException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5749249468661471065L;

	public DatabasePersistException(IOException e) {
		super(e.getMessage());
	}
	
	public DatabasePersistException(String message) {
		super(message);
	}
}
