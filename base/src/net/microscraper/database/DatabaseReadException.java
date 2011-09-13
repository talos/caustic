package net.microscraper.database;

import java.io.IOException;

/**
 * This {@link DatabaseException} is thrown when a {@link DatabaseView} cannot be
 * read.
 * @author realest
 *
 */
public class DatabaseReadException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4202317979181155168L;
	
	public DatabaseReadException(IOException e) {
		super(e.getMessage());
	}
	
	public DatabaseReadException(String message) {
		super(message);
	}
}
