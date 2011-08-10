package net.microscraper.interfaces.database;

import net.microscraper.NameValuePair;

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
	 * @return the {@link int} ID of the new row.
	 * @throws DatabaseException if the row could not be inserted.
	 */
	public abstract int insert(NameValuePair[] nameValuePairs)
			throws DatabaseException;

}