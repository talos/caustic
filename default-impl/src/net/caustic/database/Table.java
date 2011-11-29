package net.caustic.database;

import java.util.List;
import java.util.Map;

import net.caustic.scope.Scope;

/**
 * A {@link Table} interface supports reading, updating, and adding columns
 * in addition to the read functionality of {@link WritableTable}.
 * @author talos
 *
 */
public interface Table {

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
	 * @throws TableReadException if the {@link Table} cannot be checked to see whether
	 * it has the column.
	 */
	public abstract boolean hasColumn(String columnName) throws TableReadException;
	
	/**
	 * Select several columns of data from  {@link Table}.
	 * @param scope The {@link Scope} scope of the rows to select.
	 * @param whereMap A {@link Map} mapping the WHERE clause to
	 * narrow the selection.
	 * @param columnNames A {@link String} array of columns to select.
	 * @throws TableReadException if the {@link Table} cannot be read from.
	 */
	public abstract List<Map<String, String>> select(Scope scope, Map<String, String> whereMap,
					String[] columnNames)
			throws TableReadException;
	
	/**
	 * Update the rows of a scope in the {@link Table}.
	 * @param scope the {@link Scope} of the rows to update.
	 * @param whereMap A {@link Map} mapping the WHERE clause to
	 * narrow the update.  Provide an empty map to update everything in <code>
	 * scope</code> with the same values.
	 * @param updateMap A {@link Map} mapping columns names to values to update
	 * in {@link Table}.
	 * @throws TableManipulationException if the rows could not be updated.
	 */
	public abstract void update(Scope scope, Map<String, String> whereMap,
				Map<String, String> updateMap)
			throws TableManipulationException;
	
	/**
	 * Insert a new row into the {@link WritableTable}.
	 * @param scope
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link WritableTable}.
	 * @throws TableManipulationException if the row could not be inserted.
	 */
	public abstract void insert(Scope scope, Map<String, String> map) throws TableManipulationException;

	
}