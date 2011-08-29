package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

/**
 * An implementation of {@link Database} whose subclasses store
 * all results in a single table.
 * @author talos
 *
 */
public final class SingleTableDatabase implements Database {
	
	private static final String ID_COLUMN = "id";
	private static final String SOURCE_ID_COLUMN = "source_id";
	private static final String NAME_COLUMN = "name";
	private static final String VALUE_COLUMN = "value";
	
	/**
	 * Names of columns in {@link Insertable}
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		ID_COLUMN, SOURCE_ID_COLUMN, NAME_COLUMN, VALUE_COLUMN
	};
	
	/**
	 * The {@link Insertable} used by this {@link SingleTableDatabase}.
	 */
	private final Insertable table;
	
	private final HashtableDatabase hashtableDatabase = new HashtableDatabase();
	
	private final int firstId = 0;
	private int curId = firstId;
	
	private Hashtable generateMap(int id, int sourceId, String name, String value) {
		Hashtable map = new Hashtable();
		map.put(ID_COLUMN, Integer.toString(curId));
		map.put(SOURCE_ID_COLUMN, Integer.toString(sourceId));
		map.put(NAME_COLUMN, name);
		map.put(VALUE_COLUMN, value);
		return map;
	}
	
	public SingleTableDatabase(InsertableConnection connection) throws IOException {
		this.table = connection.getInsertable(COLUMN_NAMES);
	}
	
	public void close() throws IOException { }

	public int storeOneToOne(int sourceId, String name)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, ""));
		return hashtableDatabase.storeOneToOne(sourceId, name);
	}
	
	public int storeOneToOne(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, value));
		return hashtableDatabase.storeOneToOne(sourceId, name, value);
	}

	public int storeOneToMany(int sourceId, String name)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, ""));
		return hashtableDatabase.storeOneToMany(sourceId, name);
	}

	public int storeOneToMany(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, value));
		return hashtableDatabase.storeOneToMany(sourceId, name, value);
	}
	
	public String get(int id, String key) {
		
		return hashtableDatabase.get(id, key);
	}
	
	public int getFreshSourceId() throws IOException {
		curId = hashtableDatabase.getFreshSourceId();
		return curId;
	}
	
	public String toString(int id) {
		return hashtableDatabase.toString(id);
	}
}
