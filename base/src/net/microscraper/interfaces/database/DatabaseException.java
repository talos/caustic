package net.microscraper.interfaces.database;

import net.microscraper.MicroscraperException;

/**
 * Parent class for all exceptions with {@link Database}.
 * @author talos
 *
 */
public class DatabaseException extends MicroscraperException {

	public DatabaseException(Throwable e) {
		super(e);
	}
	
	public DatabaseException(String message) {
		super(message);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8677302071093634438L;
	
}