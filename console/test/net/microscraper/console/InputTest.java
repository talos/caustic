package net.microscraper.console;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.NonPersistedDatabase;
import net.microscraper.database.csv.CSVConnection;
import net.microscraper.uuid.IntUUIDFactory;

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
		database = new NonPersistedDatabase(CSVConnection.toSystemOut(','), new IntUUIDFactory());
		database.open();
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
			assertNotNull(view.get("query"));
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
		DatabaseView view;
		
		view = input.next(database);
		assertEquals("3", view.get("Borough"));
		assertEquals("1772", view.get("Block"));
		assertEquals("74", view.get("Lot"));

		view = input.next(database);
		assertEquals("1", view.get("Borough"));
		assertEquals("1171", view.get("Block"));
		assertEquals("63", view.get("Lot"));
	}

	@Test
	public void testRowsHaveCorrectValues() throws Exception {
		Input input = Input.fromSharedAndCSV(new Hashtable<String, String>(), PATH_TO_QUERIES, ',');
		input.open();
		DatabaseView view;
				
		view = input.next(database);
		assertEquals("hello", view.get("query"));

		view = input.next(database);
		assertEquals("meh", view.get("query"));
		
		view = input.next(database);
		assertEquals("bleh", view.get("query"));
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void testOpenRequired() throws Exception {
		Input input = Input.fromSharedAndCSV(
				new Hashtable<String, String>(),
				PATH_TO_QUERIES, ',');
		input.next(database);
	}

}
