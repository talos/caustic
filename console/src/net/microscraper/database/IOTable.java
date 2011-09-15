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
	 * @throws IOTableReadException if the {@link IOTable} cannot be checked to see whether
	 * it has the column.
	 */
	public abstract boolean hasColumn(String columnName) throws IOTableReadException;
	
	/**
	 * Select several columns of data from  {@link IOTable}.
	 * @param scope The {@link UUID} scope of the rows to select.
	 * @param columnNames A {@link String} array of columns to select.
	 * @throws IOTableReadException if the {@link IOTable} cannot be read from.
	 */
	public abstract List<Map<String, String>> select(UUID scope, String[] columnNames)
			throws IOTableReadException;
	
	/**
	 * Update the rows of a scope in the {@link IOTable}.
	 * @param uuid the {@link UUID} of the rows to update.
	 * @param map A {@link Hashtable} mapping columns names to values to update
	 * in {@link IOTable}.
	 * @throws TableManipulationException if the rows could not be updated.
	 */
	public abstract void update(UUID scope, Map<String, String> map)
			throws TableManipulationException;
	
}