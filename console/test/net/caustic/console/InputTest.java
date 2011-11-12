package net.caustic.console;

import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import net.caustic.console.Input;
import net.caustic.database.Database;
import net.caustic.database.NonPersistedDatabase;
import net.caustic.database.csv.CSVConnection;
import net.caustic.scope.IntScopeFactory;

import org.junit.Before;
import org.junit.Test;

public class InputTest {		
	public static final String PATH_TO_CSVS = "../fixtures/csv/";
	public static final String PATH_TO_QUERIES = PATH_TO_CSVS + "queries.csv";
	public static final String PATH_TO_BBLS = PATH_TO_CSVS + "bbls.csv";
	
	private Hashtable<String, String> shared;
	private Database database;

	@Before
	public void setUp() throws Exception {
		shared = new Hashtable<String, String>();
		for(int i = 0 ; i < 4 ; i ++) {
			shared.put(randomString(), randomString());
		}
		database = new NonPersistedDatabase(CSVConnection.toSystemOut(','), new IntScopeFactory());
		database.open();
	}
	
	@Test
	public void testFromSharedAndCSVCombinesMaps() throws Exception {
		Input input = Input.fromSharedAndCSV(shared, PATH_TO_QUERIES, ',', 0);
		input.open();
		Map<String, String> map;
		while((map = input.next()) != null) {
			for(String key : shared.keySet()) {
				assertEquals(shared.get(key), map.get(key));
			}
			assertNotNull(map.get("query"));
		}
	}

	@Test
	public void testFromSharedDoesNextOnlyOnce() throws Exception {
		Hashtable<String, String> shared = new Hashtable<String, String>();
		Input input = Input.fromShared(shared);
		input.open();
		input.next();
		assertNull(input.next());
		input.close();
	}
	
	@Test
	public void testHeadersAssignedCorrectly() throws Exception {
		Input input = Input.fromSharedAndCSV(shared, PATH_TO_BBLS, ',', 0);
		input.open();
		Map<String, String> map;
		
		map = input.next();
		assertEquals("3", map.get("Borough"));
		assertEquals("1772", map.get("Block"));
		assertEquals("74", map.get("Lot"));

		map = input.next();
		assertEquals("1", map.get("Borough"));
		assertEquals("1171", map.get("Block"));
		assertEquals("63", map.get("Lot"));
	}

	@Test
	public void testRowsHaveCorrectValues() throws Exception {
		Input input = Input.fromSharedAndCSV(new Hashtable<String, String>(), PATH_TO_QUERIES, ',', 0);
		input.open();
		Map<String, String> map;
				
		map = input.next();
		assertEquals("hello", map.get("query"));

		map = input.next();
		assertEquals("meh", map.get("query"));
		
		map = input.next();
		assertEquals("bleh", map.get("query"));
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void testOpenRequired() throws Exception {
		Input input = Input.fromSharedAndCSV(
				new Hashtable<String, String>(),
				PATH_TO_QUERIES, ',', 0);
		input.next();
	}

	@Test
	public void testSkipRows() throws Exception {
		final int rowsToSkip = 2;
		Input input = Input.fromSharedAndCSV(new Hashtable<String, String>(), PATH_TO_QUERIES, ',', rowsToSkip);
		input.open();
		Map<String, String> map;
		
		map = input.next();
		assertEquals("bleh", map.get("query"));
	}
	

	@Test(expected = IOException.class)
	public void testSkipTooManyRows() throws Exception {
		final int rowsToSkip = 4;
		Input input = Input.fromSharedAndCSV(new Hashtable<String, String>(), PATH_TO_QUERIES, ',', rowsToSkip);
		input.open(); // should throw IOException when tries to skip rows.
		
	}
}
