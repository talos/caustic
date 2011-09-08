package net.microscraper.database;

import java.io.IOException;

/**
 * Exception thrown when a modification to {@link Table} cannot be performed.
 * Extends {@link IOException}.
 * @author talos
 *
 */
public class TableManipulationException extends IOException {

	public TableManipulationException(Throwable e) {
		super(e);
	}
	
	public TableManipulationException(String message) {
		super(message);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8677302071093634438L;
	
}