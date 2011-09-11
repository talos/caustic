package net.microscraper.database;

import java.io.IOException;
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
	 * @throws IOException 
	 */
	public abstract boolean hasColumn(String columnName) throws IOException;
	
	/**
	 * Select several columns of data from  {@link IOTable}.
	 * @throws IOException 
	 */
	public abstract List<Map<String, String>> select(UUID scope, String[] columnNames) throws IOException;
	

	/**
	 * Select one column of data from  {@link IOTable}.
	 * @throws IOException 
	 */
	public abstract List<String> select(UUID scope, String columnName) throws IOException;
	
	
	/**
	 * Update an existing row in the {@link IOTable}.
	 * @param uuid the {@link UUID} of the row to update.
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link IOTable}.
	 * @throws TableManipulationException if the row could not be updated.
	 */
	public abstract void update(UUID scope, Map<String, String> map)
			throws TableManipulationException;
	
}