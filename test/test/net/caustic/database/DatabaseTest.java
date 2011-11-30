package net.caustic.database;

import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.VerificationsInOrder;
import net.caustic.database.Database;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public abstract class DatabaseTest {
	
	private ExecutorService exc;
	private Database db;
	private Scope scope;
	private Encoder encoder;
	
	public abstract Database getDatabase() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		exc = Executors.newCachedThreadPool();
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		db = getDatabase();
		scope = db.newDefaultScope();
	}

	@Test
	public void testDefaultScopeHasDefaultName() throws Exception {
		assertEquals(Database.DEFAULT_SCOPE, scope.getName());
	}
	
	@Test
	public void testNewScopeWithNameOnlyStoresNothing() throws Exception {
		Scope child = db.newScope(scope, "foo");
		
		assertNull(db.get(scope, "foo"));
		assertNull(db.get(child, "foo"));
	}

	@Test
	public void testNewScopeWithNameOnlyHasCorrectName() throws Exception {
		Scope child = db.newScope(scope, "foo");
		
		assertEquals("foo", child.getName());
	}


	@Test
	public void testNewScopeWithNameAndValueStoresOnlyInChild() throws Exception {
		Scope child = db.newScope(scope, "foo", "bar");
		
		assertNull("Parent should not have value.", db.get(scope, "foo"));
		
		String childValue = db.get(child, "foo");
		assertEquals("Child does not have correct value", "bar", childValue);
	}
	

	@Test
	public void testNewScopeWithNameAndValueHasCorrectName() throws Exception {
		Scope child = db.newScope(scope, "foo", "bar");
		
		assertEquals("foo", child.getName());
	}
	
	
	@Test
	public void testPutInScope() throws Exception {
		db.put(scope, "foo", "bar");

		assertEquals("View does not have correct value.", "bar", db.get(scope, "foo"));
	}
	

	@Test
	public void testStoreToParentAccessibleToChildren() throws Exception {
		db.put(scope, "roses", "red");
		Scope child = db.newScope(scope, "foo", "bar");
		
		assertEquals("Child should have access to parent value.", "red", db.get(child, "roses"));
	}
	
	@Test
	public void testStoreToChildNotAccessibleToParent() throws Exception {
		db.put(scope, "roses", "red");
		db.newScope(scope, "foo", "bar");
		
		assertNull("Parent should not have access to child value.", db.get(scope, "foo"));
	}
	
	@Test
	public void testPutOverwrites() throws Exception {
		db.put(scope, "foo", "bar");
		db.put(scope, "foo", "unbar");
		
		assertEquals("Overwritten value not present.", "unbar", db.get(scope, "foo"));
		assertNotSame("Old value still present.", "bar", db.get(scope, "foo"));
	}
	
	@Test
	public void testChildOverwrites() throws Exception {
		db.put(scope, "transport", "buggy");
		
		List<Scope> futures = new ArrayList<Scope>();
		for(int i = 0 ; i < 100; i ++) {
			Scope future = db.newScope(scope, "future");
			db.put(future, "transport", "shinkansen");
			futures.add(future);
		}
		
		for(Scope future : futures) {
			assertEquals("shinkansen", db.get(future, "transport"));
		}
		assertEquals("buggy", db.get(scope, "transport"));
	}

	@Test
	public void testDeepInheritence() throws Exception {
		db.put(scope, "foo", "bar");
		
		Scope child = db.newScope(scope, "layer", "onion");
		for(int i = 0 ; i < 100 ; i ++) {
			child = db.newScope(child, "layer", "onion");
		}
		
		assertEquals("bar", db.get(child, "foo"));
	}

	@Test
	public void testWideInheritence() throws Exception {
		db.put(scope, "foo", "bar");
		
		List<Scope> children = new ArrayList<Scope>();
		for(int i = 0 ; i < 100 ; i ++) {
			Scope child = db.newScope(scope, "layer");
			db.put(child, "somethin", "else");
			children.add(child);
		}
		
		for(Scope child : children) {
			assertEquals("bar", db.get(child, "foo"));
		}
	}
	
	@Test
	public void testConcurrency() throws Exception {
		Map<String, String> comparison = new HashMap<String, String>();
		for(int i = 0 ; i < 100 ; i ++) {
			final String name = randomString();
			final String value = randomString();
			comparison.put(name, value);
			exc.submit(new Callable<Void>() {
				
				public Void call() throws Exception {
					db.put(scope, name, value);
					return null;
				}
			});
		}
		
		exc.shutdown();
		if(exc.awaitTermination(10, TimeUnit.SECONDS) == false) {
			exc.shutdownNow();
			throw new InterruptedException("Didn't finish in ten seconds.");
		}
		
		for(Map.Entry<String, String> entry : comparison.entrySet()) {
			assertEquals(entry.getValue(), db.get(scope, entry.getKey()));
		}
	}
	
	
	@Test
	public void testConcurrentChildOverwrites() throws Exception {
		db.put(scope, "transport", "buggy");
		
		List<Future<Scope>> futures = new ArrayList<Future<Scope>>();
		for(int i = 0 ; i < 100; i ++) {
			futures.add(exc.submit(new Callable<Scope>() {
				public Scope call() throws Exception {
					Scope future = db.newScope(scope, randomString());
					db.put(future, "transport", "shinkansen");
					return future;
				}
			}));
		}

		exc.shutdown();
		if(exc.awaitTermination(10, TimeUnit.SECONDS) == false) {
			exc.shutdownNow();
			throw new InterruptedException("Didn't finish in ten seconds.");
		}
		
		for(Future<Scope> future : futures) {
			assertEquals("shinkansen", db.get(future.get(), "transport"));
		}
		assertEquals("buggy", db.get(scope, "transport"));
	}
	
	/*
	
	@Test
	public void testConcurrencyMultipleViews() throws Exception {
		
		final int count = 20;
		
		//List<Callable<DatabaseView>> callables = new ArrayList<Callable<DatabaseView>>();
		for(int i = 0 ; i < count ; i ++) {
			exc.submit(new Callable<Void>() {
				
				public Void call() throws DatabaseException {
					final DatabaseView view = new DatabaseView(db);
					for(int j = 0 ; j < count ; j ++) {
						final String knownKey = randomString();
						view.put(knownKey, randomString());
						view.get(knownKey);
						view.get(randomString());
						exc.submit(new Callable<Void>() {
							
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
	*/

	@Test
	public void testGetCookiesStartsEmpty() throws Exception {
		String[] cookies = db.getCookies(scope, "host.com", encoder);
		assertArrayEquals(new String[] { }, cookies);
	}

	@Test
	public void testAddCookiesInScopeDifferentHosts() throws Exception {
		db.addCookie(scope, "foo.com", "roses", "red");
		db.addCookie(scope, "bar.com", "violets", "blue");
		
		assertArrayEquals(new String[] { "roses=red" }, db.getCookies(scope, "foo.com", encoder));
		assertArrayEquals(new String[] { "violets=blue" }, db.getCookies(scope, "bar.com", encoder));
	}

	@Test
	public void testAddCookiesInScopeSameHost() throws Exception {
		db.addCookie(scope, "foo.com", "roses", "red");
		db.addCookie(scope, "foo.com", "violets", "blue");
		
		assertArrayEquals(new String[] { "roses=red", "violets=blue" },
				db.getCookies(scope, "foo.com", encoder));
	}
	
	@Test
	public void testCookiesAreEncoded() throws Exception {
		db.addCookie(scope, "foo.com", "cats == dogs", "&& parakeets, oh my!");
		
		assertArrayEquals(new String[] { "cats+%3D%3D+dogs=%26%26+parakeets%2C+oh+my%21" },
				db.getCookies(scope, "foo.com", encoder));
	}

	@Test
	public void testCookiesOverwriteInScope() throws Exception {
		db.addCookie(scope, "foo.com", "roses", "yellow");
		db.addCookie(scope, "foo.com", "roses", "red");
		
		assertArrayEquals(new String[] { "roses=red" }, db.getCookies(scope, "foo.com", encoder));
	}

	@Test
	public void testCookiesDontOverwriteOutOfScope() throws Exception {
		Scope child = db.newScope(scope, "child");
		
		db.addCookie(scope, "foo.com", "roses", "yellow");
		db.addCookie(child, "foo.com", "roses", "red");
		
		assertArrayEquals(new String[] { "roses=yellow" }, db.getCookies(scope, "foo.com", encoder));
		assertArrayEquals(new String[] { "roses=red" }, db.getCookies(child, "foo.com", encoder));
	}
	
	@Test
	public void testCookiesAccessibleInChildScope() throws Exception {
		Scope child = db.newScope(scope, "child");
		
		db.addCookie(scope, "foo.com", "roses", "red");
		db.addCookie(child, "foo.com", "violets", "blue");
		
		assertArrayEquals(new String[] { "roses=red" }, db.getCookies(scope, "foo.com", encoder));
		assertArrayEquals(new String[] { "violets=blue", "roses=red" },
				db.getCookies(child, "foo.com", encoder));
	}

	@Test
	public void testMissingInstructionsResort(final @Mocked Instruction instruction) throws Exception {
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", instruction, missingTags);
				
		ReadyExecution[] ready = db.getUnstuck(scope, "foo", "value");
		assertEquals(1, ready.length);
	}
	
	@Test
	public void testMissingInstructionsResortOnce(final @Mocked Instruction instruction) throws Exception {
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", instruction, missingTags);
				
		db.getUnstuck(scope, "foo", "bar");		
		
		ReadyExecution[] ready = db.getUnstuck(scope, "foo", "bar");
		assertEquals(0, ready.length); // already resorted
	}

	@Test
	public void testMissingInstructionsResortOnceFromPut(final @Mocked Instruction instruction) throws Exception {
		
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", instruction, missingTags);
		
		db.put(scope, "foo", "bar"); // this should call resort

		ReadyExecution[] ready = db.getUnstuck(scope, "foo", "bar");
		assertEquals(0, ready.length); // already resorted
		
	}
	
	@Test
	public void testMissingInstructionsRestart(@Mocked final DatabaseListener listener,
			final @Mocked Instruction instruction) throws Exception {
		final String[] missingTags = new String[] { "foo" };
		db.addListener(listener);

		new NonStrictExpectations() {{
			listener.onPutReady(scope, "source", instruction); times = 0;
			listener.onPutMissing(scope, "source", instruction, missingTags); times = 1;
		}};
		db.putMissing(scope, "source", instruction, missingTags);
		
		new NonStrictExpectations() {{
			listener.onPut(scope, "foo", "bar"); times = 1;
			listener.onPutReady(scope, "source", instruction); times = 1;
		}};
		db.put(scope, "foo", "bar");
	}

	@Test
	public void testMissingInstructionsRestartOnce(@Mocked final DatabaseListener listener,
			final @Mocked Instruction instruction) throws Exception {
		final String[] missingTags = new String[] { "foo" };
		db.addListener(listener);

		new NonStrictExpectations() {{
			listener.onPutReady(scope, "source", instruction); times = 0;
			listener.onPutMissing(scope, "source", instruction, missingTags); times = 1;
		}};
		db.putMissing(scope, "source", instruction, missingTags);
		
		new NonStrictExpectations() {{
			listener.onPut(scope, "foo", "bar"); times = 1;
			listener.onPutReady(scope, "source", instruction); times = 1;
		}};
		db.put(scope, "foo", "bar");
		
		new NonStrictExpectations() {{
			listener.onPut(scope, "foo", "bar"); times = 1;
			listener.onPutReady(scope, "source", instruction); times = 0;
		}};
		db.put(scope, "foo", "bar");
	}
	
	@Test
	public void testListener(@Mocked final DatabaseListener listener,
			final @Mocked Instruction instruction) throws Exception {
		
		db.addListener(listener);
		
		final Scope parent = db.newDefaultScope();
		db.put(parent, "foo", "bar");
		final Scope scope = db.newScope(parent, "roses", "red");
		db.addCookie(scope, "host.com", "name", "value");
		
		db.putReady(scope, "source", instruction);
		final String[] missingTags = new String[] { "no", "tag" };
		db.putMissing(scope, "source", instruction, missingTags);
		db.putFailed(scope, "source", instruction, "failure");
		
		new VerificationsInOrder() {{
			listener.onNewDefaultScope(parent);
			listener.onPut(parent, "foo", "bar");
			listener.onNewScope(parent, scope, "red");
			listener.onAddCookie(scope, "host.com", "name", "value");
			listener.onPutReady(scope, "source", instruction);
			listener.onPutMissing(scope, "source", instruction, missingTags);
			listener.onPutFailed(scope, "source", instruction, "failure");
		}};
	}
}
