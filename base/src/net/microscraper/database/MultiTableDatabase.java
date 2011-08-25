package net.microscraper.database;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;

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
	
	public static final String ID_COLUMN = "id";
	
	/**
	 * Column name for the name of the table where the result row's source can be
	 * found.
	 */
	public static final String SOURCE_NAME_COLUMN = "source_name";
	
	/**
	 * Default column names for {@link IOTable}s in {@link MultiTableDatabase}.
	 */
	public static final String[] COLUMN_NAMES = new String[] {
		ID_COLUMN,
		SOURCE_ID_COLUMN,
		SOURCE_NAME_COLUMN };
	
	private final Hashtable nameIds = new Hashtable();
	
	/**
	 * A {@link Hashtable} of all the {@link IOTable}s in this 
	 * {@link MultiTableDatabase}, keyed by all the IDs that could use
	 * the 
	 */
	private final Hashtable idTables = new Hashtable();
	
	/**
	 * A {@link IOConnection} to use when generating tables.
	 */
	private final IOConnection connection;
	
	private final int firstId = 0;
	private int curId = firstId;

	
	private String cleanColumnName(String columnName) {
		for(int i = 0 ; i < COLUMN_NAMES.length ; i ++) {
			if(columnName.equals(COLUMN_NAMES[i])) {
				return PREPEND + columnName;
			}
		}
		return columnName;
	}
	
	private String cleanTableName(String tableName) {
		return tableName.equals(ROOT_TABLE_NAME) ? PREPEND + tableName : tableName;
	}	
	
	private IOTable getTable(int id) throws IOException {
		if(!idTables.containsKey(Integer.valueOf(id))) {
			throw new IllegalArgumentException();
		}
		return (IOTable) idTables.get(Integer.valueOf(id));
	}
	
	private void newTable(String tableName, int id) throws IOException {
		//if(!idTables.containsKey(cleanTableName(tableName))) {
		if(nameIds.containsKey(cleanTableName(tableName))) {
			
		}
		IOTable table = this.connection.getIOTable(cleanTableName(tableName), COLUMN_NAMES);
		idTables.put(Integer.valueOf(id), table);
	}
	
	/**
	 * Create the root table with no values in it.
	 * @throws IOException If the root table cannot be created.
	 */
	public MultiTableDatabase(IOConnection connection) throws IOException {
		this.connection = connection;
		newTable(ROOT_TABLE_NAME, curId);
		//rootTable = this.connection.getIOTable(ROOT_TABLE_NAME, COLUMN_NAMES);
		//tables.put(ROOT_TABLE_NAME, rootTable);
		/*idToName.put(Integer.valueOf(firstId), ROOT_TABLE_NAME);
		tables.put(ROOT_TABLE_NAME, rootTable);*/
	}

	public int store(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		IOTable table = getTable(sourceId);
	//	table.update
	}

	public int store(int sourceId, int resultNum)
			throws TableManipulationException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int store(int sourceId, int resultNum, String name, String value)
			throws TableManipulationException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int store(Integer sourceId, int resultNum, String name, String value)
				throws IOException, TableManipulationException {
		IOTable sourceTable = getResultTable(sourceId);
		
		if(name != null && value != null) {
			updateTable(sourceTable, sourceId, name, value, resultNum);
		}
		WritableTable table = getResultTable(name);
		return table.insert(new NameValuePair[] {
				new BasicNameValuePair(SOURCE_NAME_COLUMN, sourceTable.getName()),
				new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(sourceId))
		});
	}
	/**
	 * Get a {@link IOTable} from a source ID.
	 * @param The {@link Integer} source ID number.
	 * @return The {@link IOTable}.
	 */
	private IOTable getSourceTable(Integer sourceId) throws IOException {
		
		/*String tableName = cleanTableName(resultName);
		IOTable table;
		if(tables.containsKey(tableName)) {
			table = (IOTable) tables.get(tableName);
		} else {
			table = connection.getIOTable(tableName, COLUMN_NAMES);
			tables.put(tableName, table);
		}
		
		return table;*/
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
	 * {@link Execution}.
	 * @throws TableManipulationException If the {@link IOTable} cannot be updated.
	 */
	private void updateTable(IOTable table, int id, String name, String value,
			int resultNum) throws TableManipulationException {
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
	public void clean() throws TableManipulationException { 
		Enumeration enumeration = tables.elements();
		while(enumeration.hasMoreElements()) {
			IOTable table = (IOTable) enumeration.nextElement();
			if(table.getColumnNames().length == COLUMN_NAMES.length + 1) {
				table.drop();
			}
		}
	}

	public void close() throws IOException {
		connection.close();
	}

	public int getFirstId() {
		return firstId;
	}
}
