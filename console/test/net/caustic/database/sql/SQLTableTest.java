package net.caustic.database.sql;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.caustic.database.Database;
import net.caustic.database.JDBCSqliteConnection;
import net.caustic.database.SQLConnection;
import net.caustic.database.SQLTable;
import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SQLTableTest {
	//private final SQLConnection conn;
	
	private static final Map<String, String> emptyMap = Collections.emptyMap();
	private final ScopeFactory idFactory = new IntScopeFactory();
	private final SQLConnection conn;
	
	public SQLTableTest(SQLConnection conn) throws Exception {
		this.conn = conn;
		this.conn.open();
	}
	@Parameters
	public static Collection<SQLConnection[]> implementations() {
		return Arrays.asList(new SQLConnection[][] {
			{	JDBCSqliteConnection.inMemory(Database.DEFAULT_SCOPE_NAME, false) }
		});
	}
	
	@Test
	public void testDoesNotHaveColumn() throws Exception {
		String name = randomString();
		SQLTable sqlTable = (SQLTable) conn.newIOTable(name, new String[] {}, new String[] { });
		assertFalse("Should not have column", sqlTable.hasColumn(randomString()));
	}

	@Test
	public void testHasColumnInitializedWith() throws Exception {
		String name = randomString();
		String columnName = randomString();
		SQLTable sqlTable = (SQLTable) conn.newIOTable(name,
				new String[] { columnName }, new String[] { });
		assertTrue("Should have initialized column.", sqlTable.hasColumn(columnName));
	}
	
	@Test
	public void testHasAddedColumn() throws Exception {
		String name = randomString();
		String columnName = randomString();
		SQLTable sqlTable = (SQLTable) conn.newIOTable(name, new String[] {}, new String[] { });
		sqlTable.addColumn(columnName);
		
		assertTrue("Should have added column.", sqlTable.hasColumn(columnName));
	}
	
	@Test
	public void testSelectEmptyTableReturnsNoRows() throws Exception {
		String[] columns = new String[] {
				randomString(), randomString()
		};
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });
		assertEquals("Should return no rows when no columns selected",
				0, sqlTable.select(idFactory.get(), emptyMap, new String[] {} ).size());
		assertEquals("Should return no rows when columns are selected", 0,
				sqlTable.select(idFactory.get(), emptyMap, columns ).size());
	}

	@Test
	public void testSelectSingleInsertedRow() throws Exception {
		String[] columns = new String[] {
				randomString(), randomString()
		};
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });
		Map<String, String> insertMap = new HashMap<String, String>();
		
		for(String column : columns) {
			insertMap.put(column, randomString());
		}
		
		Scope scope = idFactory.get();
		sqlTable.insert(scope, insertMap);
		
		List<Map<String, String>> selectMap = sqlTable.select(scope, emptyMap, columns);
		
		assertEquals("Should select the one inserted row", 1, selectMap.size());
		assertTrue("Selected map should be same as inserted map",
				insertMap.equals(selectMap.get(0)));
				
	}
	
	@Test
	public void testSelectSingleSeveralInsertedRows() throws Exception {
		String[] columns = new String[] {
				randomString(), randomString()
		};
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });

		Scope scope = idFactory.get();
		final int rows = 10;
		for(int i = 0 ; i < rows ; i ++) { 
			Map<String, String> insertMap = new HashMap<String, String>();
			for(String column : columns) {
				insertMap.put(column, randomString());
			}
			sqlTable.insert(scope, insertMap);
		}
		
		
		List<Map<String, String>> selectMap = sqlTable.select(scope, emptyMap, columns);
		assertEquals("Should select all inserted rows", rows, selectMap.size());
	}
	
	@Test
	public void testUpdateOneInsertedRowByScope() throws Exception {
		String[] columns = new String[] {
				randomString(), randomString()
		};
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });
		Map<String, String> insertMap = new HashMap<String, String>();
		Map<String, String> updateMap = new HashMap<String, String>();
		
		for(String column : columns) {
			insertMap.put(column, randomString());
		}

		for(String column : columns) {
			updateMap.put(column, randomString());
		}
		
		Scope scope = idFactory.get();
		sqlTable.insert(scope, insertMap);
		sqlTable.update(scope, emptyMap, updateMap);
		
		List<Map<String, String>> selectMap = sqlTable.select(scope, emptyMap, columns);

		assertEquals("Should select the one updated row", 1, selectMap.size());
		assertFalse("Selected map should not be same as originally inserted map",
				insertMap.equals(selectMap.get(0)));
		assertTrue("Selected map should be same as updated map",
				updateMap.equals(selectMap.get(0)));
	}
	

	@Test
	public void testUpdateSeveralInsertedRowsByScope() throws Exception {
		String[] columns = new String[] {
				randomString(), randomString()
		};
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });

		Scope scope = idFactory.get();
		final int rows = 10;
		for(int i = 0 ; i < rows ; i ++) { 
			Map<String, String> insertMap = new HashMap<String, String>();
			for(String column : columns) {
				insertMap.put(column, randomString());
			}
			sqlTable.insert(scope, insertMap);
		}
		
		Map<String, String> updateMap = new HashMap<String, String>();
		for(String column : columns) {
			updateMap.put(column, randomString());
		}
		
		sqlTable.update(scope, emptyMap, updateMap);
		
		List<Map<String, String>> selectMaps = sqlTable.select(scope, emptyMap, columns);
		for(int i = 0 ; i < rows ; i ++) {
			assertEquals("each row should have been updated to same values", 
						updateMap, selectMaps.get(i));
		}
	}
	

	@Test
	public void testUpdateCertainInsertedRowsByValue() throws Exception {
		final String SPECIES = "species";
		final String TEMPERAMENT = "temperament";
		final String[] columns = new String[] { SPECIES, TEMPERAMENT };
		
		SQLTable sqlTable = (SQLTable) conn.newIOTable(randomString(), columns, new String[] { });
		
		Scope scope = idFactory.get();
		
		Map<String, String> cat = new HashMap<String, String>();
		cat.put(SPECIES, "cat");
		cat.put(TEMPERAMENT, "aloof");
		
		Map<String, String> dog = new HashMap<String, String>();
		dog.put(SPECIES, "dog");
		dog.put(TEMPERAMENT, "friendly");
		
		sqlTable.insert(scope, cat);
		sqlTable.insert(scope, dog);
		
		Map<String, String> updateCat = new HashMap<String, String>();
		updateCat.put(TEMPERAMENT, "hungry");
		
		sqlTable.update(scope, cat, updateCat);
		
		assertEquals("hungry", sqlTable.select(scope, updateCat, columns).get(0).get(TEMPERAMENT));
	}
}
