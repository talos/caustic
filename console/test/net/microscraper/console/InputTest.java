package net.microscraper.console;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.microscraper.database.Database;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.HashtableDatabase;

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
		database = new HashtableDatabase();
	}
	
	@Test
	public void testFromSharedAndCSVCombinesMaps() throws Exception {
		Input input = Input.fromSharedAndCSV(shared, PATH_TO_QUERIES, ',');
		input.open();
		DatabaseView view;
		while((view = input.next(database)) != null) {
			for(String key : shared.keySet()) {
				assertEquals(shared.get(key), view.get(key));
			}
			assertNotNull(shared.get("query"));
		}
	}

	@Test
	public void testFromSharedDoesNextOnlyOnce() throws Exception {
		Hashtable<String, String> shared = new Hashtable<String, String>();
		Input input = Input.fromShared(shared);
		input.open();
		input.next(database);
		assertNull(input.next(database));
		input.close();
	}
	
	@Test
	public void testHeadersAssignedCorrectly() throws Exception {
		Input input = Input.fromSharedAndCSV(shared, PATH_TO_BBLS, ',');
		input.open();
		Map<String, String> row;
		
		row = input.next(database);
		assertTrue(row.keySet().containsAll(Arrays.asList("Borough", "Block", "Lot")));
		assertEquals("3", row.get("Borough"));
		assertEquals("1772", row.get("Block"));
		assertEquals("74", row.get("Lot"));

		row = input.next(database);
		assertTrue(row.keySet().containsAll(Arrays.asList("Borough", "Block", "Lot")));
		assertEquals("1", row.get("Borough"));
		assertEquals("1171", row.get("Block"));
		assertEquals("63", row.get("Lot"));
	}

	@Test
	public void testRowsHaveCorrectValues() throws Exception {
		Input input = Input.fromSharedAndCSV(new Hashtable<String, String>(), PATH_TO_QUERIES, ',');
		input.open();
		Map<String, String> row;
				
		row = input.next();
		assertEquals("hello", row.get("query"));
		assertEquals(1, row.size());

		row = input.next();
		assertEquals("meh", row.get("query"));
		assertEquals(1, row.size());
		
		row = input.next();
		assertEquals("bleh", row.get("query"));
		assertEquals(1, row.size());
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void testOpenRequired() throws Exception {
		Input input = Input.fromSharedAndCSV(
				new Hashtable<String, String>(),
				PATH_TO_QUERIES, ',');
		input.next();
	}

}
