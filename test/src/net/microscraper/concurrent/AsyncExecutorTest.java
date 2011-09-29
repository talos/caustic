package net.microscraper.concurrent;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AsyncExecutorTest {

	private static final int testTimeoutMs = 1000;
	private final int nThreads;
	
	private @Mocked(capture = 1) Executable executable;
	private AsyncExecutor executor;
	
	public AsyncExecutorTest(int nThreads) {
		this.nThreads = nThreads;
	}
	
	/**
	 * Test a variety of thread amounts
	 * @return
	 */
	@Parameters
	public static Collection<Integer[]> parameters() {
		return Arrays.asList(new Integer[][] { 
				{ 1 },
				{ 10 },
				{ 100 }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {{
			Executable.allAreStuck((Executable[]) any); result = false;
		}};
		executor = new AsyncExecutor(nThreads, executable);
	}

	@After
	public void tearDown() throws Exception {
		int ms = 2500;
		executor.join(ms);
		assertEquals("Executor not joining after " + ms + "ms.", false, executor.isAlive());
		executor.interrupt();
	}
	
	@Test
	public void testInterrupt() {
		executor.interrupt();
		assertEquals("Executor not interrupting.", false, executor.isAlive());
	}

	@Test
	public void isAliveAfterStart() {
		executor.start();
		assertTrue(executor.isAlive());
	}

	
	@Test
	public void testExecutesInitial() throws Exception {
		new Expectations() {{
			executable.execute();
		}};
		executor.start();
		executor.join(testTimeoutMs);
	}
	
	@Test
	public void testOneChild() throws Exception {
		new Expectations() {{
			executable.execute(); result = new Executable[] { executable }; times = 1;
			executable.execute(); result = new Executable[] { } ; times = 1;
		}};
		executor.start();
		executor.join(testTimeoutMs);
	}
	
	@Test
	public void testExecutesAllChildren()
			throws Exception {
		new Expectations() {
			{
				executable.execute(); result = new Executable[] { executable, executable, executable }; times = 1;
				executable.execute(); result = new Executable[] { } ; times = 1;
			/*	executable.execute(); result = new Executable[] { } ; times = 1;
				executable.execute(); result = new Executable[] { } ; times = 1;*/
			};
		};
		executor.start();
		executor.join(testTimeoutMs);
	}

	@Test
	public void testResubmit() {
		fail("Not yet implemented");
	}

	@Test
	public void testRecordFailure() {
		fail("Not yet implemented");
	}

}
