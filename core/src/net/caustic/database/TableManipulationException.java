package net.caustic.database;

import java.io.IOException;

/**
 * Exception thrown when a modification to {@link Table} cannot be performed.
 * Extends {@link IOException}.
 * @author talos
 *
 */
class TableManipulationException extends DatabasePersistException {
	
	public TableManipulationException(String message, Throwable e) {
		super(message, e);
	}

	public TableManipulationException(String message) {
		super(message);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8677302071093634438L;
	
}