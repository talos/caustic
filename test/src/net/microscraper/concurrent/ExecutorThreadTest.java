package net.microscraper.concurrent;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExecutorThreadTest {

	@Injectable private AsyncExecutor executor;
	@Injectable private Executable executable;
	private ExecutorThread thread;
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {{
			executable.toString(); result = "test executable";
		}};
		thread = new ExecutorThread(executor);
		thread.start();
	}

	@After
	public void tearDown() throws Exception {
		int ms = 2000;
		thread.shutdown();
		thread.join(ms);
		assertEquals("Thread still running despite join after " + ms + "ms.",
				false, thread.isAlive());
		thread.interrupt();
	}
	
	@Test
	public void testRunsIndefinitely() throws Exception {
		assertTrue(thread.isAlive());
		Thread.sleep(100);
		assertTrue(thread.isAlive());
	}

	@Test
	public void testExecute() throws Exception {
		new Expectations() {{
			executable.execute(); result = new Executable[] {}; times = 1; 
		}};
		thread.execute(executable);
		Thread.sleep(10); // wait for the ExecutorThread to start
	}
	
	@Test
	public void testExecuteSubmitsChildren() throws Exception {
		new Expectations() {
			@Injectable Executable child1, child2, child3;
			{
			executable.execute(); times = 1; result = new Executable[] { child1, child2, child3 };
			executor.submit(child1); times =1;
			executor.submit(child2); times =1;
			executor.submit(child3); times =1;
			executor.notifyFreeThread(thread); times =1;
		}};
		thread.execute(executable);
		Thread.sleep(30); // wait for the ExecutorThread to start
	}
	
	@Test
	public void testExecuteResubmitsMissingTags() throws Exception {
		new Expectations() {{
			executable.execute(); times = 1; result = null;
			executable.isMissingTags(); result = true;
			executor.resubmit(executable); times = 1;
			executor.notifyFreeThread(thread); times =1;
		}};
		thread.execute(executable);
		Thread.sleep(30); // wait for the ExecutorThread to start
	}

	@Test
	public void testExecuteNotifiesFail() throws Exception {
		final String failedBecause = "failure reason";
		new Expectations() {{
			executable.execute(); times = 1; result = null;
			executable.isMissingTags(); result = false;
			executable.getFailedBecause(); result = failedBecause;
			executor.recordFailure(failedBecause); times = 1;
			executor.notifyFreeThread(thread); times =1;
		}};
		thread.execute(executable);
		Thread.sleep(30); // wait for the ExecutorThread to start
	}
	
	@Test(expected = IllegalStateException.class)
	public void testExecuteWhenBusyThrowsIllegalState() {
		thread.execute(executable);
		thread.execute(executable);
	}
	
	@Test
	public void testIsAsleepToStart() {
		assertTrue(thread.isAsleep());
	}
	

	@Test
	public void testIsNotAsleepWhenExecuting() throws Exception {
		new Expectations() {{
			executable.execute(); times = 1; forEachInvocation = new Object() {
				void operate() throws InterruptedException {
					assertEquals(false, thread.isAsleep());
					Thread.sleep(100); // thread should not be asleep while this is happening
				}
			};
		}};
		thread.execute(executable);
		Thread.sleep(10); // wait for the ExecutorThread to start
	}
	
	@Test
	public void testExecuteOnAsleep() throws Exception {
		final int tests = 10000;
		int done = 0;
		while(done < tests) {
			if(thread.isAsleep()) {
				thread.execute(executable);
				done++;
			}
		}
	}
	
	@Test
	public void testShutdownKillsThread() throws Exception {
		thread.shutdown();
		thread.join();
		assertEquals(false, thread.isAlive());
	}
}
