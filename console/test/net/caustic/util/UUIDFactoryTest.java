package net.caustic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;
import net.caustic.uuid.JavaUtilUUIDFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UUIDFactoryTest {
	private static final int NUM_TESTS = 2000;
	private final Class<ScopeFactory> klass;
	private ScopeFactory factory;

	private static class UUIDFactoryTestRunnable implements Callable<Boolean> {
		private final ScopeFactory factory;
		private final Set<Scope> uuids;
		private final Set<String> uuidStrings;
		public UUIDFactoryTestRunnable(ScopeFactory factory, Set<Scope> uuids,
				Set<String> uuidStrings) {
			this.factory = factory;
			this.uuids = uuids;
			this.uuidStrings = uuidStrings;
		}
		
		public Boolean call() {
			for(int i = 0 ; i < NUM_TESTS ; i ++) {
				Scope uuid = factory.get();
				uuids.add(uuid);
				uuidStrings.add(uuid.asString());
			}
			return true;
		}
	}
	
	public UUIDFactoryTest(final Class<ScopeFactory> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ IntScopeFactory.class  },
				{ JavaUtilUUIDFactory.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		factory = klass.newInstance();
	}

	@Test
	public void testUniquenessInOneThread() {
		Set<Scope> uuids = new HashSet<Scope>();
		Set<String> uuidStrings = new HashSet<String>();
		for(int i = 0 ; i < NUM_TESTS ; i ++) {
			Scope uuid = factory.get();
			uuids.add(uuid);
			uuidStrings.add(uuid.asString());
		}
		assertEquals("Generated non-unique hashing UUIDs.", NUM_TESTS, uuids.size());
		assertEquals("Generated UUIDs with duplicate String values.", NUM_TESTS, uuidStrings.size());
	}
	

	@Test
	public void testUniquenessInSeveralThreads() throws Exception {
		
		final Set<Scope> uuids = Collections.synchronizedSet(new HashSet<Scope>());
		final Set<String> uuidStrings = Collections.synchronizedSet( new HashSet<String>());
		
		List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
		
		int numThreads = 100;
		ExecutorService executor = Executors.newCachedThreadPool();
		
		for(int i = 0 ; i < numThreads ; i++) {
			futures.add(executor.submit(
					new UUIDFactoryTestRunnable(factory, uuids, uuidStrings)));
		}
		
		for(Future<Boolean> future : futures) {
			assertTrue(future.get());
		}
		
		assertEquals("Generated non-unique hashing UUIDs.", NUM_TESTS * numThreads, uuids.size());
		assertEquals("Generated UUIDs with duplicate String values.", NUM_TESTS * numThreads, uuidStrings.size());
	}
}
