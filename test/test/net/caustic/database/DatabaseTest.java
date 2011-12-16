package net.caustic.database;

import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import net.caustic.Executable;
import net.caustic.Find;
import net.caustic.Load;
import net.caustic.database.Database;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public abstract class DatabaseTest {
	
	private @Mocked DatabaseListener listener;
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
		db.addListener(listener);
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
	public void testGetSingleChild() throws Exception {
		Scope child = db.newScope(scope, "joe");
		
		Scope[] children = db.getChildren(scope);
		assertEquals(1, children.length);
		assertEquals(child, children[0]);
	}
	
	@Test
	public void testGetSeveralChildren() throws Exception {
		Scope joe = db.newScope(scope, "joe");
		Scope bob = db.newScope(scope, "bob");
		Scope rachel = db.newScope(scope, "rachel");
		
		List<Scope> children = Arrays.asList(db.getChildren(scope));
		assertEquals(3, children.size());
		assertTrue(children.containsAll(Arrays.asList(joe, bob, rachel)));
	}
	
	@Test
	public void testPutInScope() throws Exception {
		db.put(scope, "foo", "bar");

		assertEquals("View does not have correct value.", "bar", db.get(scope, "foo"));
	}
	
	@Test
	public void testGetSingleResultsInScope() throws Exception {
		db.put(scope, "foo", "bar");
		
		String[][] results = db.getResults(scope);
		assertEquals(1, results.length);
		assertArrayEquals(new String[] { "foo", "bar" }, results[0]);
	}
	
	@Test
	public void testGetMultipleResultsInScope() throws Exception {
		db.put(scope, "roses", "red");
		db.put(scope, "violets", "blue");
		db.put(scope, "foo", "bar");
		
		String[][] results = db.getResults(scope);
		assertEquals(3, results.length);
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
	public void testMissingInstructionsResort() throws Exception {
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", "instruction", "uri", missingTags);
				
		StuckExecution[] ready = db.getUnstuck(scope, "foo", "value");
		assertEquals(1, ready.length);
	}
	
	@Test
	public void testMissingInstructionsResortOnce() throws Exception {
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", "instruction", "uri", missingTags);
				
		db.getUnstuck(scope, "foo", "bar");		
		
		StuckExecution[] ready = db.getUnstuck(scope, "foo", "bar");
		assertEquals(0, ready.length); // already resorted
	}

	@Test
	public void testMissingInstructionsResortOnceFromPut() throws Exception {
		
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", "instruction", "uri", missingTags);
		
		db.put(scope, "foo", "bar"); // this should call resort
		
		StuckExecution[] ready = db.getUnstuck(scope, "foo", "bar");
		assertEquals(0, ready.length); // already resorted
		
	}
	
	@Test
	public void testMissingInstructionsRestart(final @Mocked(methods = { "onPutInstruction"}, capture = 1) Database unused)
				throws Exception {
		final String[] missingTags = new String[] { "foo" };
/*
		new NonStrictExpectations() {{
			listener.onPutMissing(scope, "source", "instruction", "uri", missingTags); times = 1;
		}};*/
		db.putMissing(scope, "source", "instruction", "uri", missingTags);
		new Verifications() {{
			db.onPutInstruction(scope, "source", "instruction", "uri"); times = 0;
		}};
		
		db.put(scope, "foo", "bar");
		new Verifications() {{
			listener.onPut(scope, "foo", "bar"); times = 1;
			db.onPutInstruction(scope, "source", "instruction", "uri"); times = 1;
		}};
	}

	@Test
	public void testMissingInstructionsRestartOnce(@Mocked(methods = { "onPutInstruction" }, capture = 1) Database unused)
				throws Exception {
		final String[] missingTags = new String[] { "foo" };

		db.putMissing(scope, "source", "instruction", "uri", missingTags);
		new Verifications() {{
			db.onPutInstruction(scope, "source", "instruction", "uri"); times = 0;
			//listener.onPutMissing(scope, "source", "instruction", "uri", missingTags); times = 1;
		}};
		
		db.put(scope, "foo", "bar");
		db.put(scope, "foo", "bar");
		new Verifications() {{
			listener.onPut(scope, "foo", "bar"); times = 2;
			db.onPutInstruction(scope, "source", "instruction", "uri"); times = 1;
		}};
		
	}

	@Test
	public void testGetOneSuccess() throws Exception {
		db.putSuccess(scope, "source", "instruction", "uri");
		assertArrayEquals(new String[] { "instruction" }, db.getSuccesses(scope));
	}

	@Test
	public void testGetOneStuck() throws Exception {
		db.putMissing(scope, "source", "instruction", "uri", new String[] { "missing" });
		StuckExecution[] stuck = db.getStuck(scope);
		assertEquals(1, stuck.length);
	}

	@Test
	public void testGetOneFailed() throws Exception {
		db.putFailed(scope, "source", "instruciton", "uri", "too much beer");
		FailedExecution[] failed = db.getFailed(scope);
		assertEquals(1, failed.length);
	}
	
	@Test
	public void testBasicListener(@Mocked final Load load,
			@Mocked final Find find) throws Exception {
		
		final Scope parent = db.newDefaultScope();
		db.put(parent, "foo", "bar");
		final Scope scope = db.newScope(parent, "roses", "red");
		db.addCookie(scope, "host.com", "name", "value");
		
		// test with new scopes so we don't accidentally blow away old scope
		/*db.putInstruction(db.newDefaultScope(), "source", "instruction", "uri");
		final String[] missingTags = new String[] { "no", "tag" };
		db.putMissing(db.newDefaultScope(), "source", "instruction", "uri", missingTags);
		db.putMissing(db.newDefaultScope(), "source", find, missingTags);
		db.putMissing(db.newDefaultScope(), "source", load, missingTags);
		db.putFind(db.newDefaultScope(), "source", find);
		db.putLoad(db.newDefaultScope(), "source", load);
		db.putFailed(db.newDefaultScope(), "source", "instruction", "uri", "failure");*/
		
		new VerificationsInOrder() {{
			listener.onNewDefaultScope(parent);
			listener.onPut(parent, "foo", "bar");
			listener.onNewScope(parent, scope, "red");
			/*listener.onAddCookie(scope, "host.com", "name", "value");
			listener.onPutInstruction((Scope) any, "source", "instruction", "uri");
			listener.onPutMissing((Scope) any, "source", "instruction", "uri", missingTags);
			listener.onPutMissing((Scope) any, "source", null, null, missingTags); // find
			listener.onPutMissing((Scope) any, "source", null, null, missingTags); // load
			listener.onPutFailed((Scope) any, "source", "instruction", "uri", "failure");*/
		}};
	}
	
	@Test
	public void testListenerScopeCompleteOnSuccess() 
			throws Exception {
		
		
		final Scope scope = db.newDefaultScope();
		
		db.putInstruction(scope, "source", "instruction", "uri");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, anyInt, anyInt, anyInt); times = 0;
		}};
		
		db.putSuccess(scope, "source", "instruction", "uri");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, 1, 0, 0); times = 1;
		}};
	}
	
	@Ignore
	@Test(expected = NullPointerException.class)
	public void testListenerScopeCompleteClearsScope() 
			throws Exception {
		
		final Scope scope = db.newDefaultScope();
		db.put(scope, "foo", "bar");
		
		assertEquals("bar", db.get(scope, "foo"));
		db.putInstruction(scope, "source", "instruction", "uri");
		db.putSuccess(scope, "source", "instruction", "uri");
		
		db.get(scope, "foo"); // should blow up
	}
	
	@Test
	public void testListenerScopeCompleteOnMissing() 
			throws Exception {
		
		final Scope scope = db.newDefaultScope();
		
		db.putInstruction(scope, "source", "instruction", "uri");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, anyInt, anyInt, anyInt); times = 0;
		}};
		
		db.putMissing(scope, "source", "instruction", "uri", new String[] { "missing" });
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, 0, 1, 0); times = 1;
		}};
	}

	@Test
	public void testListenerScopeCompleteOnFailed() 
			throws Exception {
		
		final Scope scope = db.newDefaultScope();
		
		db.putInstruction(scope, "source", "instruction", "uri");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, anyInt, anyInt, anyInt); times = 0;
		}};
		
		db.putFailed(scope, "source", "instruction", "uri", "FAIL");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(scope, 0, 0, 1); times = 1;
		}};
	}
	
	@Test
	public void testListenerScopeCompleteFromChildren()
			throws Exception {
		final Scope outer = db.newDefaultScope();
		final Scope inner = db.newScope(outer, "child");
		
		db.putInstruction(outer, "source", "foo", "uri");
		db.putInstruction(inner, null, "bar", "uri");

		db.putSuccess(outer, "source", "foo", "uri");
		
		// the outer scope should stay alive because 
		// the inner one is not yet resolved
		new Verifications() {{
			listener.onScopeComplete(outer, anyInt, anyInt, anyInt); times = 0;
			listener.onScopeComplete(inner, anyInt, anyInt, anyInt); times = 0;
		}};		
		
		db.putSuccess(inner, null, "bar", "uri");
		
		new VerificationsInOrder() {{
			listener.onScopeComplete(inner, 1, 0, 0); times = 1;
			listener.onScopeComplete(outer, 1, 0, 0); times = 1;
		}};
	}
	
	@Test
	public void testGetSinglePausedLoad(@Mocked final Executable executable)
			throws Exception {
		
		db.putPausedLoad(scope, executable);
		Executable[] paused = db.getPaused(scope);
		assertEquals(1, paused.length);
		assertEquals(executable, paused[0]);
	}

	@Test
	public void testGetSinglePausedLoadDisappears(@Mocked final Executable executable)
			throws Exception {
		
		db.putPausedLoad(scope, executable);
		
		new Expectations() {{
			executable.run();
			executable.hasBeenRun(); result = true;
		}};
		executable.run();
		Executable[] paused = db.getPaused(scope);
		assertEquals(0, paused.length);
	}
	
	@Test
	public void testGetSeveralPausedLoads(@Mocked final Executable a,
				@Mocked final Executable b, @Mocked final Executable c)
			throws Exception {
		
		db.putPausedLoad(scope, a);
		db.putPausedLoad(scope, b);
		db.putPausedLoad(scope, c);
		List<Executable> paused = Arrays.asList(db.getPaused(scope));
		assertEquals(3, paused.size());
		assertTrue(paused.containsAll(Arrays.asList(a, b, c)));
	}
	

	@Test
	public void testGetSeveralPausedLoadsDisappear(@Mocked final Executable a,
				@Mocked final Executable b, @Mocked final Executable c)
			throws Exception {
		
		db.putPausedLoad(scope, a);
		db.putPausedLoad(scope, b);
		db.putPausedLoad(scope, c);
		
		new Expectations() {{
			a.run();
			b.run();
			a.hasBeenRun(); result = true;
			b.hasBeenRun(); result = true;
			c.hasBeenRun(); result = false;
		}};
		a.run();
		b.run();
		Executable[] paused = db.getPaused(scope);
		assertEquals(1, paused.length);
		assertEquals(c, paused[0]);
	}
}
