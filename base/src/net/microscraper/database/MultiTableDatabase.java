package net.microscraper.database;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.instruction.Result;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Utils;

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
	 * Default column names for {@link IOTable}s in {@link MultiTableDatabase}.
	 */
	public static final String[] COLUMN_NAMES = new String[] {
		SOURCE_ID_COLUMN,
		SOURCE_NAME_COLUMN };
	
	/**
	 * The {@link IOTable} that holds {@link Result}s that don't have
	 * a source.  These would be from the first layer of
	 * {@link Instruction}s.  This table has only one row.
	 */
	private final IOTable rootTable;
	
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
	 * A {@link IOConnection} to use when generating tables.
	 */
	private final IOConnection connection;
	
	/**
	 * Create the root table with no values in it.
	 * @throws DatabaseException If the root table cannot be created.
	 */
	public MultiTableDatabase(IOConnection connection) throws DatabaseException {
		this.connection = connection;
		rootTable = this.connection.getIOTable(ROOT_TABLE_NAME, COLUMN_NAMES);
		tables.put(ROOT_TABLE_NAME, rootTable);
		rootResultId = rootTable.insert(new NameValuePair[] {});
	}
	
	public int store(String name, String value, int resultNum) throws DatabaseException {
		
		if(value != null) {
			updateTable(rootTable, rootResultId, name, value, resultNum);	
		}
		WritableTable table = getResultTable(name);
		return table.insert(new NameValuePair[] {
				new BasicNameValuePair(SOURCE_NAME_COLUMN, ROOT_TABLE_NAME),
				new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(rootResultId))
		});
	}
	
	public int store(String sourceName, int sourceId, String name, String value, int resultNum) throws DatabaseException {
		String sourceTableName = cleanTableName(sourceName);
		IOTable sourceTable;
		if(tables.containsKey(sourceTableName)) {
			sourceTable = (IOTable) tables.get(sourceTableName);
		} else {
			sourceTable = getResultTable(sourceTableName);
			tables.put(sourceTableName, sourceTable);
		}
		
		if(value != null) {
			updateTable(sourceTable, sourceId, name, value, resultNum);
		}
		WritableTable table = getResultTable(name);
		return table.insert(new NameValuePair[] {
				new BasicNameValuePair(SOURCE_NAME_COLUMN, sourceTable.getName()),
				new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(sourceId))
		});
	}
	
	private String cleanColumnName(String columnName) {
		for(int i = 0 ; i < COLUMN_NAMES.length ; i ++) {
			if(columnName.equals(COLUMN_NAMES[i])) {
				return PREPEND + columnName;
			}
		}
		return columnName;
	}
	
	private String cleanTableName(String tableName) {
		if(tableName.equals(ROOT_TABLE_NAME)) {
			return PREPEND + tableName;
		} else {
			return tableName;
		}
	}
	
	/**
	 * Get a {@link IOTable} for a {@link Result} name.
	 * @param resultName The {@link Result} name to get the {@link IOTable} for.
	 * @return The {@link IOTable}.
	 * @throws DatabaseException If there was an error generating the {@link IOTable}.
	 */
	private IOTable getResultTable(String resultName) throws DatabaseException {
		String tableName = cleanTableName(resultName);
		IOTable table;
		if(tables.containsKey(tableName)) {
			table = (IOTable) tables.get(tableName);
		} else {
			table = connection.getIOTable(tableName, COLUMN_NAMES);
			tables.put(tableName, table);
		}
		
		return table;
	}
	
	/**
	 * Update a {@link IOTable} with a <code>name</code> and <code>value</code>,
	 * using {@link #PREPEND} and adding a column if necessary.
	 * @param table The {@link IOTable} to update.
	 * @param id The {@link int} id of the row to update.
	 * @param name The {@link String} name of the column to update, this will be
	 * prepended with {@link #PREPEND} and added as a new column to <code>table</code>
	 * if it is not yet there.
	 * @param value The {@link String} value to update.  Should <b>not</b> be <code>
	 * null</code>.
	 * @param the 0-based {@link int} index of this {@link Result} within its
	 * {@link Executable}.
	 * @throws DatabaseException If the {@link IOTable} cannot be updated.
	 */
	private void updateTable(IOTable table, int id, String name, String value,
			int resultNum) throws DatabaseException {
		String columnName = cleanColumnName(name);
		if(resultNum > 0) {
			columnName = Integer.toString(resultNum) + columnName;
		}
		if(!table.hasColumn(columnName)) {
			table.addColumn(columnName);
		}
		table.update(id, new NameValuePair[] {
			new BasicNameValuePair(columnName, value) }
		);
	}
	
	/**
	 * Drop tables that never had additional columns added -- they were from
	 * {@link Instruction}s that were not saved.
	 */
	public void close() throws DatabaseException { 
		Enumeration enumeration = tables.elements();
		while(enumeration.hasMoreElements()) {
			IOTable table = (IOTable) enumeration.nextElement();
			if(table.getColumnNames().length == COLUMN_NAMES.length + 1) {
				table.drop();
			}
		}
		connection.close();
	}
}
