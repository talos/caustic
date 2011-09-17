package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.microscraper.database.csv.CSVConnection;
import net.microscraper.database.sql.JDBCSqliteConnection;
import net.microscraper.uuid.IntUUIDFactory;
import net.microscraper.uuid.JavaUtilUUIDFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


//@RunWith(Parameterized.class)
public class DatabaseViewTest {
	
	private ExecutorService exc;
	private static final char DELIMITER = ',';
//	private final Database db;
	private DatabaseView view;
	private Database db;

	public DatabaseViewTest() throws Exception {
		db = new MultiTableDatabase(JDBCSqliteConnection.inMemory(Database.SCOPE_COLUMN_NAME),
				new IntUUIDFactory() );
		db.open();
	}
	/*
	public DatabaseViewTest(Database db) throws Exception {
		this.db = db;
		db.open();
	}
	/*
	@Parameters
	public static List<Database[]> implementations() {
		return Arrays.asList(new Database[][] {
				{ new NonPersistedDatabase(CSVConnection.toSystemOut(DELIMITER),
						new IntUUIDFactory() )  },
				{ new SingleTableDatabase(JDBCSqliteConnection.inMemory(Database.SCOPE_COLUMN_NAME),
						new IntUUIDFactory() )  },
				{ new MultiTableDatabase(JDBCSqliteConnection.inMemory(Database.SCOPE_COLUMN_NAME),
						new IntUUIDFactory() )  }
		});
	}
	*/
	@Before
	public void setUp() throws Exception {
		//db.open();
		view = db.newView();
		exc = Executors.newCachedThreadPool();
	}
	
	@After
	public void tearDown() throws Exception {
		db.close();
	}

	@Test
	public void testSpawnChildWithNameOnlyStoresNothing() throws Exception {
		String name = randomString();
		DatabaseView child = view.spawnChild(name);

		assertNull(view.get(name));
		assertNull(child.get(name));
	}

	@Test
	public void testSpawnChildWithNameAndValueStoresOnlyInChild() throws Exception {
		String name = randomString();
		String value = randomString();
		
		DatabaseView child = view.spawnChild(name, value);
		
		assertNull("Parent should not have value.", view.get(name));
		
		String childValue = child.get(name);
		assertEquals("Child does not have correct value", value, childValue);
	}
	
	@Test
	public void testStoreStoresInView() throws Exception {
		String name = randomString();
		String value = randomString();
		
		view.put(name, value);

		assertEquals("View does not have correct value.", value, view.get(name));
	}
	

	@Test
	public void testStoreToParentAccessibleToChildren() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		view.put(name, value);
		assertEquals("Child should have access to parent value.", value, child.get(name));
	}
	

	@Test
	public void testStoreToChildNotAccessibleToParent() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		child.put(name, value);
		assertNull("Parent should not have access to child value.", view.get(name));
	}
	
	@Test
	public void testPutOverwrites() throws Exception {
		
		String name = randomString();
		String value = randomString();
		String value2 = randomString();
		
		view.put(name, value);
		view.put(name, value2);
		
		assertEquals("Overwritten value not present.", value2, view.get(name));
		assertNotSame("Old value still present.", value, view.get(name));
	}
	
	@Test
	public void testConcurrentPut() throws Exception {

		final String name = randomString();
		final String value = randomString();
		
		exc.submit(new Callable<Void>() {
			@Override
			public Void call() throws DatabasePersistException {
				view.put(name, value);
				return null;
			}
		});
		
		exc.submit(new Callable<String>() {
			@Override
			public String call() throws DatabaseReadException {
				return view.get(name);
			}
		});
		
		exc.shutdown();
		exc.awaitTermination(3, TimeUnit.SECONDS);
		assertEquals(value, view.get(name));
	}
	

	@Test
	public void testConcurrentSpawnChildren() throws Exception {
		
		final String name = randomString();
		final String value = randomString();
		
		final String nameToOverwrite = randomString();
		final String overwrittenValue = randomString();
		final String overwritingValue = randomString();
		
		final String childName = randomString();
		final String childValue = randomString();
		view.put(name, value);
		view.put(nameToOverwrite, overwrittenValue);
		final int threads = 150;
		
		List<Callable<DatabaseView>> callables = new ArrayList<Callable<DatabaseView>>();
		for(int i = 0 ; i < threads ; i ++) {
			callables.add(new Callable<DatabaseView>() {
				@Override
				public DatabaseView call() throws DatabasePersistException {
					DatabaseView child = view.spawnChild(childName, childValue);
					child.put(nameToOverwrite, overwritingValue);
					return child;
				}
			});
		}
		List<Future<DatabaseView>> futures = exc.invokeAll(callables);
		
		assertEquals("should not be overwritten in original view.", overwrittenValue, view.get(nameToOverwrite));
		assertNull("should not have child name/value in original view", view.get(childName));
		for(Future<DatabaseView> future : futures) {
			DatabaseView child = future.get();
			assertFalse("should have been overwritten", overwrittenValue.equals(child.get(nameToOverwrite)));
			assertEquals("not overwritten with expected value", overwritingValue, child.get(nameToOverwrite));
			assertEquals("child should have its name/value", childValue, child.get(childName));
		}
	}
}
