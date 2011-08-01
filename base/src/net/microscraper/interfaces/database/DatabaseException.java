package net.microscraper.interfaces.database;

import net.microscraper.ClientException;

/**
 * Parent class for all exceptions with {@link Database}.
 * @author talos
 *
 */
public class DatabaseException extends ClientException {

	public DatabaseException(Throwable e) {
		super(e);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8677302071093634438L;
	
}