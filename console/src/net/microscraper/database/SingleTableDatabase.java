package net.microscraper.database;

import java.util.HashMap;
import java.util.Map;

import net.microscraper.uuid.UUID;

public class SingleTableDatabase {

	protected void insert(WritableTable table, UUID id, UUID source, String name, String value) throws TableManipulationException {
		Map<String, String> insertMap = new HashMap<String, String>();
		if(source != null) {
			insertMap.put(SOURCE_COLUMN_NAME, source.asString());
		}
		insertMap.put(NAME_COLUMN_NAME, name);
		if(value != null) {
			insertMap.put(VALUE_COLUMN_NAME, value);
		}
		
		table.insert(id, insertMap);
	}
	
	public static final String TABLE_NAME = "result";
	
	public static final String SOURCE_COLUMN_NAME = "source";
	public static final String NAME_COLUMN_NAME = "name";
	public static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Names of columns in the table.
	 */
	public static final String[] COLUMN_NAMES = new String[] {
		SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
}
