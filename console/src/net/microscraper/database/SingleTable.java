package net.microscraper.database;

import java.util.Collections;
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
	
	/**
	 * Insert the row.  Assumes <code>scope</code> is not unique.
	 * @param table
	 * @param scope
	 * @param optSource
	 * @param name
	 * @param optValue
	 * @throws TableManipulationException
	 */
	public static void insert(WritableTable table,
			UUID scope, UUID optSource, String name, String optValue) throws TableManipulationException {
		Map<String, String> insertMap = new HashMap<String, String>();
		if(optSource != null) {
			insertMap.put(SOURCE_COLUMN_NAME, optSource.asString());
		}
		insertMap.put(NAME_COLUMN_NAME, name);
		if(optValue != null) {
			insertMap.put(VALUE_COLUMN_NAME, optValue);
		}
		
		table.insert(scope, insertMap);
	}

	/**
	 * Update the row based off of <code>scope</code>
	 * and <code>name</code>.  Insert it otherwise.
	 * @param table
	 * @param scope
	 * @param optSource
	 * @param name
	 * @param optValue
	 * @throws DatabasePersistException
	 */
	public static void replace(IOTable table,
			UUID scope, UUID optSource, String name, String optValue) throws DatabasePersistException {
		Map<String, String> updateMap = new HashMap<String, String>();
		updateMap.put(NAME_COLUMN_NAME, name);
		if(optSource != null) {
			updateMap.put(SOURCE_COLUMN_NAME, optSource.asString());
		}
		if(optValue != null) {
			updateMap.put(VALUE_COLUMN_NAME, optValue);
		}
		
		Map<String, String> whereMap = new HashMap<String, String>();
		whereMap.put(NAME_COLUMN_NAME, name);
		
		try {
			if(table.select(scope, whereMap,
					new String[] { NAME_COLUMN_NAME }).size() == 0) { // insert the row
				table.insert(scope, updateMap);
			} else { // update the existing row.
				table.update(scope, whereMap, updateMap);
			}
		} catch (IOTableReadException e) {
			throw new DatabasePersistException(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param table
	 * @param scope
	 * @param name
	 * @return the value of <code>name</code> in <code>scope</code> or its nearest
	 * source, <code>null</code> if neither it nor its sources have a value for
	 * <code>name</code> (or if its <code>value</code> is <code>null</code>).
	 * @throws DatabaseReadException
	 */
	public static String select(IOTable table, UUID scope, String name) throws DatabaseReadException {
		Map<String, String> whereMap = Collections.emptyMap();
		
		// return all name-values in this scope
		List<Map<String, String>> rows = table.select(scope, whereMap, 
				new String[] { SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME } );
		
		// look for the row with the name we want, return as soon as it's found.
		UUID sourceId = null;
		for(Map<String, String> row : rows) {
			if(row.get(NAME_COLUMN_NAME).equals(name)) {
				return row.get(VALUE_COLUMN_NAME);
			} else if(row.get(SOURCE_COLUMN_NAME) != null) {
				sourceId = new DeserializedUUID(row.get(SOURCE_COLUMN_NAME));
			}
		}
		
		// if we found a sourceID, use it as another scope to search in; otherwise, return null.
		if(sourceId != null) {
			return select(table, sourceId, name);
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @return A {@link SingleTable} {@link WritableTable}.
	 * @throws ConnectionException
	 */
	public static WritableTable get(WritableConnection connection) throws ConnectionException {
		return connection.newWritable(TABLE_NAME, COLUMN_NAMES);
	}
	
	/**
	 * 
	 * @param connection
	 * @return A {@link SingleTable} {@link IOTable}.
	 * @throws ConnectionException
	 */
	public static IOTable get(IOConnection connection) throws ConnectionException {
		IOTable table = connection.getIOTable(TABLE_NAME);
		if(table != null) {
			return table;
		} else {
			return connection.newIOTable(TABLE_NAME, COLUMN_NAMES);
		}
	}
}
