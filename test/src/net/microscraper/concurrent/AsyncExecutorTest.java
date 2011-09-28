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

	private final int nThreads;
	
	private @Mocked Executable executable;
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
			executable.toString(); result = "executable";
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
		executor.join(100);
	}
	
	@Test
	public void testSubmitOnce() throws Exception {
		new Expectations() {{
			executable.execute(); times = 2;
		}};
		executor.start();
		executor.submit(executable);
		executor.join(1000);
	}
	
	@Test
	public void testOneChild() throws Exception {
		new Expectations() {
			@Injectable Executable child;
			{
			executable.execute(); result = new Executable[] { child }; times = 1;
			child.execute(); result = null; times = 1;
			//anotherExecutable.execute();
		}};
		executor.start();
		executor.submit(executable);
		executor.join(1000);
	}
	@Test
	public void testExecutesAllSubmissions(@Mocked final Executable child1,
					@Mocked final Executable child2,
					@Mocked final Executable child3)
			throws Exception {
		new Expectations() {
			{
			executable.execute(); result = new Executable[] { child1, child2, child3 }; times = 1;
			executable.execute(); result = null; times = 3;
		}};
		executor.start();
		executor.submit(child1);
		executor.submit(child2);
		executor.submit(child3);
		executor.join(500);
	}
	
	@Test
	public void testExecutesAllChildren()
			throws Exception {
		final int submissions = 10;
		final Executable[] children = new Executable[submissions];
		for(int i = 0 ; i < children.length ; i ++) {
			children[i] = executable;
		}
		new Expectations() {
			@Injectable Executable child1, child2, child3;

			{
				executable.execute(); result = new Executable[] { child1, child2, child3 }; times = 1;
				executable.execute(); result = null; times = 3;
			};
		};
		executor.start();
		executor.join(500);
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
