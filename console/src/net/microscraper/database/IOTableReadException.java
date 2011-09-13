package net.microscraper.database;

import java.io.IOException;

import net.microscraper.database.sql.SQLConnectionException;

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

	public IOTableReadException(IOException e) {
		super(e);
	}

	public IOTableReadException(SQLConnectionException e) {
		super(e.getMessage());
	}
}
