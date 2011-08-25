package net.microscraper.database;

import net.microscraper.util.NameValuePair;

/**
 * A table that supports the insertion of new rows & generation of IDs.
 * @author talos
 * @see #insert(NameValuePair[])
 *
 */
public interface WritableTable {

	/**
	 * Insert a new row into the {@link IOTable}.
	 * @param nameValuePairs An array of {@link NameValuePair}s mapping
	 * columns to values to insert.
	 * @throws TableManipulationException if the row could not be inserted.
	 */
	public abstract void insert(NameValuePair[] nameValuePairs)
			throws TableManipulationException;
}