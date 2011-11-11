package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.internal.expectations.transformation.ExpectationsTransformer;
import net.microscraper.database.csv.CSVConnection;
import net.microscraper.database.sql.JDBCSqliteConnection;
import net.microscraper.uuid.IntUUIDFactory;
import net.microscraper.uuid.JavaUtilUUIDFactory;
import net.microscraper.uuid.UUIDFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class DatabaseViewTest {
	
	@Mocked("out") private System mockOut;
	private ExecutorService exc;
	private static final char DELIMITER = ',';
	private final Database db;
	private DatabaseView view;
	
	public DatabaseViewTest(Constructor<Database> dbConstructor,
			Method connStaticConstructor, List<Object> connStaticArgs,
			UUIDFactory idFactory) throws Exception {
		Connection conn = (Connection) connStaticConstructor.invoke(null, connStaticArgs.toArray());
		db = dbConstructor.newInstance(conn, idFactory);
		db.open();
	}
	
	@Parameters
	public static List<Object[]> implementations() throws Exception {
		// Reflection, yay! :/
		// this must be done because @Parameters must be static, and this wrecks with opening connections.
		return Arrays.asList(new Object[][] {
				{ InMemorySingleTableDatabase.class.getConstructor(WritableConnection.class, UUIDFactory.class),
					CSVConnection.class.getMethod("toSystemOut", char.class), Arrays.asList(DELIMITER),
					new IntUUIDFactory() },
				{ PersistedSingleTableDatabase.class.getConstructor(IOConnection.class, UUIDFactory.class),
					JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
					Arrays.asList(Database.SCOPE_COLUMN_NAME, true), new IntUUIDFactory() },
				{ PersistedMultiTableDatabase.class.getConstructor(IOConnection.class, UUIDFactory.class),
						JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
						Arrays.asList(Database.SCOPE_COLUMN_NAME, true), new IntUUIDFactory() },
				{ PersistedMultiTableDatabase.class.getConstructor(IOConnection.class, UUIDFactory.class),
					JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
					Arrays.asList(Database.SCOPE_COLUMN_NAME, false), new JavaUtilUUIDFactory() },
		});
	}
	
	@Before
	public void setUp() throws Exception {
		//db.open();
		//db.open();
		view = db.newView();
		exc = Executors.newCachedThreadPool();
	}
	
	@After
	public void tearDown() throws Exception {
		//db.close();
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
	public void testChildOverwrites() throws Exception {
		final String name = "parent name";
		final String value = "parent value";
		
		final String nameToOverwrite = "transport";
		final String overwrittenValue = "buggy";
		final String overwritingValue = "shinkanesn";
		
		final String childName = "child name";
		final String childValue = "child value";
		view.put(name, value);
		view.put(nameToOverwrite, overwrittenValue);
		
		List<DatabaseView> children = new ArrayList<DatabaseView>();
		for(int i = 0 ; i < 100; i ++) {
			DatabaseView child = view.spawnChild(childName, childValue);
			child.put(nameToOverwrite, overwritingValue);
			children.add(child);
		}
		
		for(DatabaseView child : children) {
			assertFalse(nameToOverwrite + " should have been overwritten with " + overwritingValue +
				", but is still " + overwrittenValue, overwrittenValue.equals(child.get(nameToOverwrite)));
			assertEquals("not overwritten with expected value", overwritingValue, child.get(nameToOverwrite));
			assertEquals("child should have its name/value", childValue, child.get(childName));
		}
	}
	
	@Test
	public void testConcurrentPut() throws Exception {

		final String name = randomString();
		final String value = randomString();
		
		exc.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				view.put(name, value);
				return null;
			}
		});
		
		exc.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return view.get(name);
			}
		});
		
		exc.shutdown();
		exc.awaitTermination(3, TimeUnit.SECONDS);
		assertEquals(value, view.get(name));
	}
	

	@Test
	public void testConcurrentChildOverwrites() throws Exception {
		
		final String name = "parent name";
		final String value = "parent value";
		
		final String nameToOverwrite = "transport";
		final String overwrittenValue = "buggy";
		final String overwritingValue = "shinkanesn";
		
		final String childName = "child name";
		final String childValue = "child value";
		view.put(name, value);
		view.put(nameToOverwrite, overwrittenValue);
		final int count = 20;
		
		List<Callable<DatabaseView>> callables = new ArrayList<Callable<DatabaseView>>();
		for(int i = 0 ; i < count ; i ++) {
			callables.add(new Callable<DatabaseView>() {
				@Override
				public DatabaseView call() throws Exception {
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
			assertFalse(nameToOverwrite + " should have been overwritten with " + overwritingValue +
					", but is still " + overwrittenValue, overwrittenValue.equals(child.get(nameToOverwrite)));
			assertEquals("not overwritten with expected value", overwritingValue, child.get(nameToOverwrite));
			assertEquals("child should have its name/value", childValue, child.get(childName));
		}
	}

	@Test
	public void testConcurrencyMultipleViews() throws Exception {
		
		final int count = 20;
		
		//List<Callable<DatabaseView>> callables = new ArrayList<Callable<DatabaseView>>();
		for(int i = 0 ; i < count ; i ++) {
			exc.submit(new Callable<Void>() {
				@Override
				public Void call() throws DatabaseException {
					final DatabaseView view = db.newView();
					for(int j = 0 ; j < count ; j ++) {
						final String knownKey = randomString();
						view.put(knownKey, randomString());
						view.get(knownKey);
						view.get(randomString());
						exc.submit(new Callable<Void>() {
							@Override
							public Void call() throws DatabaseException {
								view.put(randomString(), randomString());
								DatabaseView child = view.spawnChild(randomString(), randomString());
								for(int k = 0 ; k < count ; k ++) {
									child.get(knownKey);
									child.put(randomString(), randomString());
									child.get(randomString());
									child.get(knownKey);
									view.get(knownKey);
									view.put(randomString(), randomString());
									view.get(randomString());
								}
								return null;
							}
						});
					}
					return null;
				}
			});
		}
		//futures = exc.invokeAll(callables);
		exc.shutdown();
		assertTrue("didn't complete in one minute", exc.awaitTermination(1, TimeUnit.MINUTES));
	}
	
	@Test
	public void testHook(@Mocked(capture = 1) final DatabaseViewListener hook) throws Exception {
		
		view.addListener(hook);
		view.put("key", "value");
		view.spawnChild("name");
		view.spawnChild("name", "value");
		
		new Verifications() {{
			hook.put("key", "value");
			hook.spawnChild("name", (DatabaseView) any);
			hook.spawnChild("name", "value", (DatabaseView) any);
		}};
	}
}
