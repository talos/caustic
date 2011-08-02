package net.microscraper.impl.database;

import java.util.Hashtable;

import net.microscraper.BasicNameValuePair;
import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.database.Connection;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.Table;

/**
 * An implementation of {@link Database} whose subclasses store
 * {@link Result}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public final class MultiTableDatabase implements Database {
	
	/**
	 * String to prepend before table names to prevent collision
	 * with {@link #ROOT_TABLE_NAME}, and to prepend before column
	 * names to prevent collision with anything in {@link #COLUMN_NAMES}.
	 */
	public static final char PREPEND = '_';
	
	/**
	 * Name of {@link #rootTable}.
	 */
	public static final String ROOT_TABLE_NAME = "root";
	
	/**
	 * Column name for the ID of the source of a result row.
	 */
	public static final String SOURCE_ID_COLUMN = "source_id";
	
	/**
	 * Column name for the name of the table where the result row's source can be
	 * found.
	 */
	public static final String SOURCE_NAME_COLUMN = "source_name";
	
	/**
	 * Default column names for {@link Table}s in {@link MultiTableDatabase}.
	 */
	public static final String[] COLUMN_NAMES = new String[] { SOURCE_ID_COLUMN, SOURCE_NAME_COLUMN };
	
	/**
	 * The {@link Table} that holds {@link Result}s that don't have
	 * a source.  These would be from the first layer of
	 * {@link Instruction}s.  This table has only one row.
	 */
	private final Table rootTable;
	
	/**
	 * The {@link int} ID for the only row of {@link #rootTable}.
	 */
	private final int rootResultId;
	
	/**
	 * A {@link Hashtable} of all the {@link ChildResultsTable}s in this 
	 * {@link MultiTableDatabase}, excepting {@link #rootTable}.
	 * Keyed by {@link String} {@link ChildResultsTable} names.
	 */
	private final Hashtable tables = new Hashtable();
	
	/**
	 * A {@link Connection} to use when generating tables.
	 */
	private final Connection connection;
	
	/**
	 * Create the root table with no values in it.
	 * @throws DatabaseException If the root table cannot be created.
	 */
	public MultiTableDatabase(Connection connection) throws DatabaseException {
		this.connection = connection;
		rootTable = this.connection.getTable(ROOT_TABLE_NAME, COLUMN_NAMES);
		rootResultId = rootTable.insert(new NameValuePair[] {});
	}
	
	public Result store(String name, String value) throws DatabaseException {
		updateTable(rootTable, rootResultId, name, value);
		Table table = getResultTable(name);
		return new Result(table.insert(new NameValuePair[] {
				new BasicNameValuePair(SOURCE_NAME_COLUMN, ROOT_TABLE_NAME),
				new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(rootResultId))
		}), name, value);
	}
	
	public Result store(Result source, String name, String value) throws DatabaseException {
		String sourceTableName = PREPEND + source.getName();
		Table sourceTable;
		if(tables.containsKey(sourceTableName)) {
			sourceTable = (Table) tables.get(sourceTableName);
		} else {
			sourceTable = getResultTable(sourceTableName);
			tables.put(sourceTableName, sourceTable);
		}
		
		updateTable(sourceTable, source.getId(), name, value);
		Table table = getResultTable(name);
		return new Result(table.insert(new NameValuePair[] {
				new BasicNameValuePair(SOURCE_NAME_COLUMN, sourceTable.getName()),
				new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(source.getId()))
		}), name, value);
	}
	
	/**
	 * Get a {@link Table} for a {@link Result} name.
	 * @param resultName The {@link Result} name to get the {@link Table} for.
	 * @return The {@link Table}.
	 * @throws DatabaseException If there was an error generating the {@link Table}.
	 */
	private Table getResultTable(String resultName) throws DatabaseException {
		String tableName = PREPEND + resultName;
		Table table;
		if(tables.containsKey(tableName)) {
			table = (Table) tables.get(tableName);
		} else {
			table = connection.getTable(tableName, COLUMN_NAMES);
			tables.put(tableName, table);
		}
		
		return table;
	}
	
	/**
	 * Update a {@link Table} with a <code>name</code> and <code>value</code>,
	 * using {@link #PREPEND} and adding a column if necessary.
	 * @param table The {@link Table} to update.
	 * @param id The {@link int} id of the row to update.
	 * @param name The {@link String} name of the column to update, this will be
	 * prepended with {@link #PREPEND} and added as a new column to <code>table</code>
	 * if it is not yet there.
	 * @param value The {@link String} value to update.
	 * @throws DatabaseException If the {@link Table} cannot be updated.
	 */
	private void updateTable(Table table, int id, String name, String value) throws DatabaseException {
		String columnName = PREPEND + name;
		if(!table.hasColumn(columnName)) {
			table.addColumn(columnName);
		}
		table.update(id, new NameValuePair[] {
			new BasicNameValuePair(columnName, value) }
		);
	}
}
