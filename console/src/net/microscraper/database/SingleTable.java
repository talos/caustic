package net.microscraper.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.uuid.DeserializedUUID;
import net.microscraper.uuid.UUID;

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
	

	public static String select(IOTable table, UUID scope, String name) throws IOException {
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
	
	public static WritableTable get(WritableConnection connection) throws IOException {
		return connection.newWritable(TABLE_NAME, COLUMN_NAMES);
	}

	public static IOTable get(IOConnection connection) throws IOException {
		return connection.newIOTable(TABLE_NAME, COLUMN_NAMES);
	}
}
