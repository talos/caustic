package net.microscraper.database;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.microscraper.console.UUID;

/**
 * A {@link IOTable} interface supports reading, updating, and adding columns
 * in addition to the read functionality of {@link WritableTable}.
 * @author talos
 *
 */
public interface IOTable extends WritableTable {

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
	 * @return A {@link String} array of the names of columns in the {@link IOTable}.
	 */
	public abstract String[] getColumnNames();
	

	/**
	 * Select rows of data from {@link IOTable}.
	 * @param idColumnName the {@link String} name of the ID column.
	 * @param uuid the {@link UUID} of the row to update.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link IOTable}.
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract Map<UUID, Map<String, String>> select(String idColumnName, UUID uuid,
						List<String> columnNames)
			throws TableManipulationException;
	
	/**
	 * Update an existing row in the {@link IOTable}.
	 * @param idColumnName the {@link String} name of the ID column.
	 * @param uuid the {@link UUID} of the row to update.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link IOTable}.
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract void update(String idColumnName, UUID uuid, Map<String, String> map)
			throws TableManipulationException;
	
	/**
	 * Delete this {@link IOTable}.
	 * @throws TableManipulationException If the {@link IOTable} could not be dropped.
	 */
	//public void drop() throws TableManipulationException;
	
}