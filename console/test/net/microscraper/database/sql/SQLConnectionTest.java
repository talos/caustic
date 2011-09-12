package net.microscraper.database.sql;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.database.IOTable;
import net.microscraper.uuid.DeserializedUUID;
import net.microscraper.uuid.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SQLConnectionTest {
	private final int BATCH_SIZE = 1;
	private SQLConnection conn;
	
	@Before
	public void setUp() throws Exception {
		conn = JDBCSqliteConnection.inMemory(BATCH_SIZE);
		conn.open();
	}
	
	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	@Test
	public void testIOTable() throws Exception {
		int numColumns = 4;
		
		UUID scope = new DeserializedUUID(randomString());
		String name = randomString();
		String[] columns = new String[numColumns];
		Map<String, String> map = new HashMap<String, String>();
		
		for(int i = 0 ; i < numColumns ; i ++) {
			columns[i] = randomString(5);
			map.put(columns[i], randomString());
		}
		IOTable table = conn.newIOTable(name, columns);
		
		for(String column : columns) {
			assertTrue(table.hasColumn(column));
		}
		
		table.insert(scope, map);
		
		List<Map<String, String>> selectedMaps = table.select(scope, columns);
		assertEquals("Should only return one row", 1, selectedMaps.size());
		for(Map<String, String> selectedMap : selectedMaps) {
			assertTrue("Selected map missing key(s).", selectedMap.keySet().containsAll(map.keySet()));
			assertTrue("Selected map missing value(s).", selectedMap.values().containsAll(map.values()));
		}
		
		for(String column : columns) {
			assertTrue(table.hasColumn(column));
			List<String> selectedColumn = table.select(scope, column);
			assertEquals("Should only return one row.", 1, selectedColumn.size());
			for(String value : selectedColumn) {
				assertEquals("Unexpected value retrieved.", map.get(column), value);
			}
		}
		
		String addColumn = randomString();
		String addValue = randomString();
		Map<String, String> addColumnMap = new HashMap<String, String>();
		addColumnMap.put(addColumn, addValue);
		
		table.addColumn(addColumn);
		table.update(scope, addColumnMap);
		assertEquals("Should have updated row", 1, table.select(scope, addColumn).size());
		assertEquals("Should have populated added column", addValue, table.select(scope, addColumn).get(0));
	}

}
