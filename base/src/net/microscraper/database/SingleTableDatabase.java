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

	public void clean() throws TableManipulationException { }

	public int store(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, value));
		return sourceId;
	}
	
	public int store(int sourceId, int resultNum)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, "", ""));
		return curId;
	}

	public int store(int sourceId, int resultNum, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(generateMap(curId, sourceId, name, value));
		return curId;
	}
	
	public int getFirstId() {
		return firstId;
	}
}
