package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.console.UUID;

/**
 * A {@link Updateable} interface supports adding columns updating certain columns by ID,
 * and deleting.  It extends {@link Insertable}.
 * @author talos
 *
 */
public interface Updateable extends Insertable {

	/**
	 * Add a column.
	 * @param columnName The {@link String} name of column to add.
	 * @throws TableManipulationException if the column could not be added.
	 */
	public abstract void addColumn(String columnName) throws TableManipulationException;

	/**
	 * Check whether a column exists already.
	 * @param columnName The {@link String} name of the column to check.
	 * @return <code>true</code> if the column exists, <code>false</code> otherwise.
	 */
	public abstract boolean hasColumn(String columnName);
	
	/**
	 * 
	 * @return A {@link String} array of the names of columns in the {@link Updateable}.
	 */
	public abstract String[] getColumnNames();

	/**
	 * Update an existing row in the {@link Updateable}.
	 * @param idColumnName the {@link String} name of the ID column.
	 * @param uuid the {@link UUID} of the row to update.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link Updateable}.
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract void update(String idColumnName, UUID uuid, Hashtable map)
			throws TableManipulationException;
	
	/**
	 * Delete this {@link Updateable}.
	 * @throws TableManipulationException If the {@link Updateable} could not be dropped.
	 */
	public void drop() throws TableManipulationException;
	
}