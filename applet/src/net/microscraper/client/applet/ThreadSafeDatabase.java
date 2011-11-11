package net.microscraper.client.applet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.caustic.database.Database;
import net.caustic.database.TableManipulationException;

public class ThreadSafeDatabase implements Database {
	public static final String ID = "id";
	public static final String SOURCE_ID = "source_id";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	
	private final List<Map<String, String>> executions = Collections.synchronizedList(new ArrayList<Map<String, String>>());
	
	private int lastId = -1;
	private Integer readPos = -1;

	public void resetIterator() {
		synchronized(readPos) {
			readPos = -1;
		}
	}
	public boolean hasNext() {
		synchronized(executions) {
			synchronized(readPos) {
				return executions.size() > readPos + 1;
			}
		}
	}
	public Map<String, String> next() {
		synchronized(executions) {
			synchronized(readPos) {
				readPos++;
				return executions.get(readPos);
			}
		}
	}
	
	private Map<String, String> getEntry(Integer sourceId, String name, String value) {
		lastId++;
		Map<String, String> entry = new HashMap<String, String>();
		
		entry.put(ID, Integer.toString(lastId));
		entry.put(SOURCE_ID, Integer.toString(sourceId));
		entry.put(NAME, name);
		entry.put(VALUE, value);
		
		return entry;
	}
	
	@Override
	public int storeInitial(String name, String value, int resultNum)
			throws IOException, TableManipulationException {
		executions.add(getEntry(null, name, value));
		return lastId;
	}
	@Override
	public int store(int sourceId, int resultNum, String name,
			String sourceName, String value) throws IOException,
			TableManipulationException {
		executions.add(getEntry(sourceId, name, value));
		return lastId;
	}
	@Override
	public void clean() throws TableManipulationException {
		
	}
	@Override
	public void close() throws IOException {
		
	}
}
