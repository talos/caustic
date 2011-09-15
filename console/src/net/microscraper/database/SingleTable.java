package net.microscraper.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.uuid.DeserializedUUID;
import net.microscraper.uuid.UUID;

/**
 * A set of static functions to persist a {@link Database} in a single table.
 * @author talos
 *
 */
class SingleTable {	
	public static final String TABLE_NAME = "result";
	
	public static final String SOURCE_COLUMN_NAME = "source";
	public static final String NAME_COLUMN_NAME = "name";
	public static final String VALUE_COLUMN_NAME = "value";
	
	private SingleTable() { }
	
	/**
	 * Names of columns in the table.
	 */
	public static final String[] COLUMN_NAMES = new String[] {
		SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
	
	public static void insert(WritableTable table,
			UUID scope, UUID source, String name, String value) throws TableManipulationException {
		Map<String, String> insertMap = new HashMap<String, String>();
		if(source != null) {
			insertMap.put(SOURCE_COLUMN_NAME, source.asString());
		}
		insertMap.put(NAME_COLUMN_NAME, name);
		if(value != null) {
			insertMap.put(VALUE_COLUMN_NAME, value);
		}
		
		
		table.insert(scope, insertMap);
	}

	public static void update(IOTable table,
			UUID scope, UUID source, String name, String value) throws DatabasePersistException {
		Map<String, String> map = new HashMap<String, String>();
		map.put(NAME_COLUMN_NAME, name);
		if(source != null) {
			map.put(SOURCE_COLUMN_NAME, source.asString());
		}
		if(value != null) {
			map.put(VALUE_COLUMN_NAME, value);
		}
		
		try {
			if(table.select(scope, new String[] { NAME_COLUMN_NAME }).size() == 0) { // insert the row
				table.insert(scope, map);
			} else { // update the existing row.
				table.update(scope, map);
			}
		} catch (IOTableReadException e) {
			throw new DatabasePersistException(e.getMessage());
		}
	}
	
	public static String select(IOTable table, UUID scope, String name) throws DatabaseReadException {
		List<Map<String, String>> rows = table.select(scope,
				new String[] { SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME } );
		
		UUID sourceId = null;
		for(Map<String, String> row : rows) {
			if(row.get(NAME_COLUMN_NAME).equals(name)) {
				return row.get(VALUE_COLUMN_NAME);
			} else if(row.get(SOURCE_COLUMN_NAME) != null) {
				sourceId = new DeserializedUUID(row.get(SOURCE_COLUMN_NAME));
			}
		}
		if(sourceId != null) {
			return select(table, sourceId, name);
		} else {
			return null;
		}
	}
	
	public static WritableTable get(WritableConnection connection) throws ConnectionException {
		return connection.newWritable(TABLE_NAME, COLUMN_NAMES);
	}
	
	public static IOTable get(IOConnection connection) throws ConnectionException {
		IOTable table = connection.getIOTable(TABLE_NAME);
		if(table != null) {
			return table;
		} else {
			return connection.newIOTable(TABLE_NAME, COLUMN_NAMES);
		}
	}
}
