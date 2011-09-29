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
	
	
	/**
	 * Shutdown the test thread and wait for it to finish up, ensuring expectations
	 * are met in the @Test block.
	 */
	private void shutdownAndJoin() throws Exception {
		int ms = 2000;
		thread.shutdown();
		thread.join(ms);
		assertEquals("Thread still running despite join after " + ms + "ms.",
				false, thread.isAlive());
	}
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {{
			executable.toString(); result = "test executable";
		}};
		thread = new ExecutorThread(executor);
	}

	@After
	public void tearDown() throws Exception {
		thread.interrupt();
	}
	
	@Test
	public void testRunsIndefinitely() throws Exception {
		synchronized(thread) {
			thread.start();
			assertTrue(thread.isAlive());
			Thread.sleep(100);
			assertTrue(thread.isAlive());
		}
		shutdownAndJoin();
	}

	@Test
	public void testExecute() throws Exception {
		new Expectations() {{
			// return empty array to mimic successful execution.
			executable.execute(); result = new Executable[] { } ; times = 1; 
		}};
		thread.start();
		thread.execute(executable);
		
		shutdownAndJoin();
	}


	@Test
	public void testNotifiesOfFreeThread() throws Exception {
		new Expectations() {{
			executor.notifyFreeThread(thread); times = 1; 
		}};
		thread.start();
		
		Thread.sleep(100);
		shutdownAndJoin();
	}
	

	@Test
	public void testNotifiesOfFreeThreadBeforeAndAfterExecuteWhileAlive() throws Exception {
		new Expectations() {{
			executor.notifyFreeThread(thread); times = 1; 
			executable.execute(); result = new Executable[] { } ; times = 1;
			executor.notifyFreeThread(thread); times = 1; 
		}};
		thread.start();
		
		Thread.sleep(100);
		thread.execute(executable);
		Thread.sleep(100);
		
		shutdownAndJoin();
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

		}};
		thread.start();
		thread.execute(executable);

		shutdownAndJoin();
	}
	
	@Test
	public void testExecuteResubmitsMissingTags() throws Exception {
		new Expectations() {{
			
			executable.execute(); result = null; times = 1; result = null;
			executable.isMissingTags(); result = true;
			executor.resubmit(executable); times = 1; $ = "If tags are missing, should notify executor.";
			
		}};
		thread.start();
		thread.execute(executable);

		shutdownAndJoin();
	}

	@Test
	public void testExecuteNotifiesFail() throws Exception {
		final String failedBecause = "failure reason";
		new Expectations() {{
			
			executable.execute(); times = 1; result = null;
			executable.isMissingTags(); result = false;
			executable.getFailedBecause(); result = failedBecause;
			executor.recordFailure(failedBecause); times = 1;
			
		}};
		thread.start();
		thread.execute(executable);

		shutdownAndJoin();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testExecuteWhenBusyThrowsIllegalState() throws Exception {
		thread.start();
		thread.execute(executable);
		thread.execute(executable);

		shutdownAndJoin();
	}
}
