package net.caustic.database.sql;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.caustic.database.Database;
import net.caustic.database.Table;
import net.caustic.database.sql.JDBCSqliteConnection;
import net.caustic.database.sql.SQLConnection;
import net.caustic.scope.SerializedScope;
import net.caustic.scope.Scope;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SQLConnectionTest {
	private static final Map<String, String> emptyMap = Collections.emptyMap();
	private SQLConnection conn;
	
	@Before
	public void setUp() throws Exception {
		conn = JDBCSqliteConnection.inMemory(Database.DEFAULT_SCOPE_NAME, false);
		conn.open();
	}
	
	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	@Test
	public void testIOTable() throws Exception {
		int numColumns = 4;
		
		Scope scope = new SerializedScope(randomString());
		String name = randomString();
		String[] columns = new String[numColumns];
		Map<String, String> map = new HashMap<String, String>();
		
		for(int i = 0 ; i < numColumns ; i ++) {
			columns[i] = randomString(5);
			map.put(columns[i], randomString());
		}
		Table table = conn.newIOTable(name, columns, new String[] { });
		
		for(String column : columns) {
			assertTrue(table.hasColumn(column));
		}
		
		table.insert(scope, map);
		conn.commit();
		
		List<Map<String, String>> selectedMaps = table.select(scope, emptyMap, columns);
		assertEquals("Should only return one row", 1, selectedMaps.size());
		for(Map<String, String> selectedMap : selectedMaps) {
			assertTrue("Selected map missing key(s).", selectedMap.keySet().containsAll(map.keySet()));
			assertTrue("Selected map missing value(s).", selectedMap.values().containsAll(map.values()));
		}
		
		for(String column : columns) {
			assertTrue(table.hasColumn(column));
			List<Map<String, String>> rows = table.select(scope, emptyMap, new String[] { column } );
			assertEquals("Should only return one row.", 1, rows.size());
			for(Map<String, String> row : rows) {
				assertEquals("Unexpected value retrieved.", map.get(column), row.get(column));
			}
		}
		
		String addColumn = randomString();
		String addValue = randomString();
		Map<String, String> addColumnMap = new HashMap<String, String>();
		addColumnMap.put(addColumn, addValue);
		
		System.out.println("adding column");
		table.addColumn(addColumn);
		
		System.out.println("updating added column value");
		table.update(scope, emptyMap,addColumnMap);
		conn.commit();
		
		assertEquals("Should have updated row", 1, table.select(scope,emptyMap,
				new String[] { addColumn }).size());
		
		System.out.println("should have populated...");
		assertEquals("Should have populated added column", addValue,
				table.select(scope, emptyMap,new String[] { addColumn } ).get(0).get(addColumn));
	}

}
