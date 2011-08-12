package net.microscraper.interfaces.database;

import net.microscraper.NameValuePair;

/**
 * A {@link IOTable} interface that supports adding columns and inserting & updating
 * rows with a single {@link int} id.
 * @author talos
 *
 */
public interface IOTable extends WritableTable {

	/**
	 * 
	 * @return The {@link String} name of the {@link IOTable}.
	 */
	public String getName();
	
	/**
	 * Add a column.
	 * @param columnName The {@link String} name of column to add.
	 * @throws DatabaseException if the column could not be added.
	 */
	public abstract void addColumn(String columnName) throws DatabaseException;

	/**
	 * Check whether a column exists already.
	 * @param columnName The {@link String} name of the column to check.
	 * @return <code>true</code> if the column exists, <code>false</code> otherwise.
	 */
	public abstract boolean hasColumn(String columnName);
	
	/**
	 * 
	 * @return A {@link String} array of the names of columns in the {@link IOTable}.
	 */
	public abstract String[] getColumnNames();

	/**
	 * Update an existing row in the {@link IOTable}.
	 * @param id the {@link int} ID of the row to update.
	 * @param nameValuePairs An array of {@link NameValuePair}s mapping
	 * columns to new values.
	 * @throws DatabaseException if the row could not be updated.
	 */
	public abstract void update(int id, NameValuePair[] nameValuePairs)
			throws DatabaseException;
	
	/**
	 * Delete this {@link IOTable}.
	 * @throws DatabaseException If the {@link IOTable} could not be dropped.
	 */
	public void drop() throws DatabaseException;
}