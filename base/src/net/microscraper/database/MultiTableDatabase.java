package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

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
	public static final String SOURCE_ID_COLUMN_NAME = "source_id";
	
	public static final String ID_COLUMN_NAME = "id";
	
	/**
	 * Column name for the name of the table where the result row's source can be
	 * found.
	 */
	public static final String SOURCE_TABLE_COLUMN = "source_table";
	
	public static final String VALUE_COLUMN_NAME = "value";
	
	public static final String[] ROOT_TABLE_COLUMNS = new String[] {
		ID_COLUMN_NAME
	};
	
	/**
	 * Default column names for {@link Updateable}s in {@link MultiTableDatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] {
		ID_COLUMN_NAME,
		SOURCE_ID_COLUMN_NAME,
		SOURCE_TABLE_COLUMN,
		VALUE_COLUMN_NAME
	};
	
	private final Hashtable nameTables = new Hashtable();
	
	/**
	 * A {@link Hashtable} of the name of every ID value.
	 */
	private final Hashtable idNames = new Hashtable();
	
	/**
	 * A {@link UpdateableConnection} to use when generating tables.
	 */
	private final UpdateableConnection connection;
	
	private final HashtableDatabase hashtableDatabase = new HashtableDatabase();
	
	private final int firstId = 0;
	private int curId = firstId;

	
	private String cleanColumnName(String columnName) {
		for(int i = 0 ; i < RESULT_TABLE_COLUMNS.length ; i ++) {
			if(columnName.equals(RESULT_TABLE_COLUMNS[i])) {
				return PREPEND + columnName;
			}
		}
		return columnName;
	}
	
	private String cleanTableName(String tableName) {
		return tableName.equals(ROOT_TABLE_NAME) ? PREPEND + tableName : tableName;
	}	
	
	/**
	 * Create the root table with no values in it.
	 * @throws IOException If the root table cannot be created.
	 */
	public MultiTableDatabase(UpdateableConnection connection) throws IOException {
		this.connection = connection;
	}
	
	public int storeOneToOne(int sourceId, String name)
			throws TableManipulationException, IOException {
		// No-op, would just enter in a column with a blank value.
		return hashtableDatabase.storeOneToOne(sourceId, name);
	}
	
	/**
	 * Add this name & value to the table of sourceId.  Create the column for the name
	 * if it doesn't already exist.
	 */
	public int storeOneToOne(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		//curId ++;
		String tableName = (String) idNames.get(Integer.valueOf(sourceId));
		Updateable table = (Updateable) nameTables.get(tableName);
		if(!table.hasColumn(cleanColumnName(name))) {
			table.addColumn(cleanColumnName(name));
		}
		Hashtable map = new Hashtable();
		map.put(cleanColumnName(name), value);
		
		table.update(ID_COLUMN_NAME, sourceId, map);
		
		return hashtableDatabase.storeOneToOne(sourceId, name, value);
	}

	public int storeOneToMany(int sourceId, String name)
			throws TableManipulationException, IOException {
		curId ++;
		
		String sourceTableName = (String) idNames.get(Integer.valueOf(sourceId));
		
		Updateable table;
		idNames.put(Integer.valueOf(curId), cleanTableName(name));
		if(nameTables.containsKey(cleanTableName(name))) {
			table = (Updateable) nameTables.get(cleanTableName(name));
		} else {
			table = connection.getIOTable(cleanTableName(name), RESULT_TABLE_COLUMNS);
			nameTables.put(cleanTableName(name), table);
		}
		
		Hashtable map = new Hashtable();
		map.put(ID_COLUMN_NAME, Integer.toString(curId));
		map.put(SOURCE_ID_COLUMN_NAME, Integer.toString(sourceId));
		map.put(SOURCE_TABLE_COLUMN, sourceTableName);
		map.put(VALUE_COLUMN_NAME, "");
		table.insert(map);
		
		return hashtableDatabase.storeOneToMany(sourceId, name);
	}

	/**
	 * Insert a new row into a table for this result.
	 */
	public int storeOneToMany(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId ++;
		
		String sourceTableName = (String) idNames.get(Integer.valueOf(sourceId));
		
		Updateable table;
		idNames.put(Integer.valueOf(curId), cleanTableName(name));
		if(nameTables.containsKey(cleanTableName(name))) {
			table = (Updateable) nameTables.get(cleanTableName(name));
		} else {
			table = connection.getIOTable(cleanTableName(name), RESULT_TABLE_COLUMNS);
			nameTables.put(cleanTableName(name), table);
		}
		
		Hashtable map = new Hashtable();
		map.put(ID_COLUMN_NAME, Integer.toString(curId));
		map.put(SOURCE_ID_COLUMN_NAME, Integer.toString(sourceId));
		map.put(SOURCE_TABLE_COLUMN, sourceTableName);
		map.put(VALUE_COLUMN_NAME, value);
		table.insert(map);
		
		return hashtableDatabase.storeOneToMany(sourceId, name, value);
	}
	
	public void close() throws IOException {
		connection.close();
	}

	public String get(int id, String key) {
		return hashtableDatabase.get(id, key);
	}

	public Variables open() throws IOException {
		Updateable rootTable = connection.getIOTable(ROOT_TABLE_NAME, ROOT_TABLE_COLUMNS);
		idNames.put(Integer.valueOf(firstId), ROOT_TABLE_NAME);
		nameTables.put(ROOT_TABLE_NAME, rootTable);
		hashtableDatabase.open();
		return new Variables(this, curId);
	}

	public String toString(int id) {
		return hashtableDatabase.toString(id);
	}
}
