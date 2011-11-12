package net.caustic.database;

import static net.caustic.util.TestUtils.randomString;
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
import net.caustic.database.Connection;
import net.caustic.database.Database;
import net.caustic.database.IOConnection;
import net.caustic.database.InMemoryDatabase;
import net.caustic.database.PersistedMultiTableDatabase;
import net.caustic.database.PersistedSingleTableDatabase;
import net.caustic.database.WritableConnection;
//import net.caustic.database.csv.CSVConnection;
//import net.caustic.database.sql.JDBCSqliteConnection;
import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;
import net.caustic.uuid.JavaUtilUUIDFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class DatabaseTest {
	
	@Mocked("out") private System mockOut;
	private ExecutorService exc;
	private static final char DELIMITER = ',';
	private final Class<Database> klass;
	private Database db;
	private DatabaseView view;
	
	/*public DatabaseTest(Constructor<Database> dbConstructor,
			Method connStaticConstructor, List<Object> connStaticArgs,
			ScopeFactory idFactory) throws Exception {
		Connection conn = (Connection) connStaticConstructor.invoke(null, connStaticArgs.toArray());
		db = dbConstructor.newInstance(conn, idFactory);
		//db.open();
	}*/
	public DatabaseTest(Class<Database> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static List<Object[]> implementations() throws Exception {
		return Arrays.asList(new Object[][] {
				{ InMemoryDatabase.class }	
		});
		
		// Reflection, yay! :/
		// this must be done because @Parameters must be static, and this wrecks with opening connections.
		/*return Arrays.asList(new Object[][] {
				{ InMemoryDatabase.class.getConstructor(WritableConnection.class, ScopeFactory.class),
					CSVConnection.class.getMethod("toSystemOut", char.class), Arrays.asList(DELIMITER),
					new IntScopeFactory() },
				{ PersistedSingleTableDatabase.class.getConstructor(IOConnection.class, ScopeFactory.class),
					JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
					Arrays.asList(Database.DEFAULT_SCOPE_NAME, true), new IntScopeFactory() },
				{ PersistedMultiTableDatabase.class.getConstructor(IOConnection.class, ScopeFactory.class),
						JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
						Arrays.asList(Database.DEFAULT_SCOPE_NAME, true), new IntScopeFactory() },
				{ PersistedMultiTableDatabase.class.getConstructor(IOConnection.class, ScopeFactory.class),
					JDBCSqliteConnection.class.getMethod("inMemory", Class.forName("java.lang.String"), boolean.class),
					Arrays.asList(Database.DEFAULT_SCOPE_NAME, false), new JavaUtilUUIDFactory() },
		});*/
	}
	
	@Before
	public void setUp() throws Exception {
		//db.open();
		//db.open();
		//view = db.newView();
		exc = Executors.newCachedThreadPool();
		db = klass.newInstance();
		view = new DatabaseView(db);
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
		view.put("roses", "red");
		DatabaseView child = view.spawnChild("foo", "bar");
		
		assertEquals("Child should have access to parent value.", "red", child.get("roses"));
	}
	
	@Test
	public void testStoreToChildNotAccessibleToParent() throws Exception {
		view.put("roses", "red");
		DatabaseView child = view.spawnChild("foo", "bar");
		
		assertNull("Parent should not have access to child value.", view.get("foo"));
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
					final DatabaseView view = new DatabaseView(db);
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
	public void testListener(@Mocked(capture = 1) final DatabaseListener listener) throws Exception {
		
		db.addListener(listener);
		
		final Scope parent = db.newScope();
		db.put(parent, "foo", "bar");
		final Scope child = db.newScope(parent, "roses", "red");
		
		new Verifications() {{
			listener.newScope(parent);
			listener.put(parent, "foo", "bar");
			listener.newScope(parent, "roses", "red", child);
		}};
	}
}
