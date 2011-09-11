package net.microscraper.database;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.microscraper.uuid.UUID;

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
	 * Select several columns of data from  {@link IOTable}.
	 * 
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract List<Map<String, String>> select(String id, String[] columnNames);
	

	/**
	 * Select one column of data from  {@link IOTable}.
	 * 
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract List<String> select(String id, String columnName);
	
	
	/**
	 * Update an existing row in the {@link IOTable}.
	 * @param uuid the {@link UUID} of the row to update.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link IOTable}.
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract void update(UUID uuid, Map<String, String> map)
			throws TableManipulationException;
	
}