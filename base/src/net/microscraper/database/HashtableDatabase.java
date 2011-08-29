package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.util.StringUtils;

/**
 * {@link HashtableDatabase} hold information collected through the execution of {@link Instruction}s,
 * as well as default values.
 * @author talos
 *
 */
public class HashtableDatabase implements Database {
	
	/**
	 * A {@link Hashtable} of {@link Integer} source IDs keyed by {@link Integer} ID.
	 */
	private final Hashtable idSources = new Hashtable();
	
	/**
	 * A {@link Hashtable} of {@link Hashtable}s keyed by {@link Integer} ID.
	 */
	private final Hashtable idTables = new Hashtable();
	
	private final int firstId = -1;
	private int curId = firstId;

	public int storeOneToOne(int sourceId, String name)
			throws TableManipulationException, IOException {
		//curId++;
		// No-op: can't put null value in a hashtable.
		//Hashtable table = (Hashtable) idTables.get(Integer.valueOf(sourceId));
		//table.put(name, null);
		return sourceId;
	}

	public int storeOneToOne(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		//curId++;
		Hashtable table = (Hashtable) idTables.get(Integer.valueOf(sourceId));
		table.put(name, value);
		return sourceId;
	}

	public int storeOneToMany(int sourceId, String name)
			throws TableManipulationException, IOException {
		curId++;
		Hashtable table = new Hashtable();
		// table.put(name, null);
		idSources.put(Integer.valueOf(curId), Integer.valueOf(sourceId));
		idTables.put(Integer.valueOf(curId), table);
		return curId;
	}

	public int storeOneToMany(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		Hashtable table = new Hashtable();
		table.put(name, value);
		idSources.put(Integer.valueOf(curId), Integer.valueOf(sourceId));
		idTables.put(Integer.valueOf(curId), table);
		return curId;
	}

	public String get(int id, String key) {
		Hashtable table = (Hashtable) idTables.get(Integer.valueOf(id));
		if(table.containsKey(key)) {
			return (String) table.get(key);
		} else if(idSources.containsKey(Integer.valueOf(id))) {
			Integer parentId = (Integer) idSources.get(Integer.valueOf(id));
			return get(parentId.intValue(), key);
		} else {
			return null;
		}
	}
	
	public String toString(int id) {
		String result = "";
		Integer sourceId = Integer.valueOf(id);
		while(idSources.containsKey(sourceId)) {
			Hashtable table = (Hashtable) idTables.get(sourceId);
			result += StringUtils.quote(table.toString()) + " << ";
			sourceId = (Integer) idSources.get(sourceId);
		}
		return result;
	}
	
	public void close() throws IOException {
		idSources.clear();
		idTables.clear();
		curId = firstId;
	}

	public int getFreshSourceId() throws IOException {
		curId++;
		idTables.put(Integer.valueOf(curId), new Hashtable());
		return curId;
	}
}
