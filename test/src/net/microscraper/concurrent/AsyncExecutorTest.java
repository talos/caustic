package net.microscraper.concurrent;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AsyncExecutorTest {

	private final int nThreads;
	
	@Injectable Executable executable;
	AsyncExecutor executor;
	
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
		executor = new AsyncExecutor(nThreads, executable);
	}

	@After
	public void breakDown() throws Exception {
		executor.join();
		assertEquals("Executor should not be alive.", false, executor.isAlive());
		//executor.interrupt();
	}
	
	@Test
	public void testInterrupt() {
		// done in breakdown
	}

	@Test
	public void testExecutesInitial() throws Exception {
		new Expectations() {{
			executable.execute();
		}};
		executor.start();
		executor.join();
	}

	@Test
	public void testSubmit(@Mocked final Executable anotherExecutable) throws Exception {
		new Expectations() {{
			anotherExecutable.execute();
		}};
		executor.start();
		executor.submit(anotherExecutable);
		executor.join();
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
