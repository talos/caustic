package net.microscraper.database;

import java.util.Hashtable;

/**
 * An interface for writing via {@link #insert(Hashtable)}.
 * @author talos
 *
 */
public interface Insertable {

	/**
	 * Insert a new row into the {@link Updateable}.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link Insertable}.
	 * @throws TableManipulationException if the row could not be inserted.
	 */
	public abstract void insert(Hashtable map) throws TableManipulationException;
}