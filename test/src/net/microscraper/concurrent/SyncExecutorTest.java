package net.microscraper.concurrent;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Before;
import org.junit.Test;

public class SyncExecutorTest {
	@Mocked() Executable executable;
	
	SyncExecutor executor;
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {{
			Executable.allAreStuck((Executable[]) any); result = false;
		}};
		executor = new SyncExecutor();
	}

	@Test
	public void testRunExecutes() throws Exception {
		new Expectations() {{
			executable.execute(); times = 1;
		}};
		executor.execute(executable);
	}

	@Test
	public void testRunsAllChildren() throws Exception {
		new NonStrictExpectations() {
			@Mocked Executable child1, child2, child3;
			{
				executable.execute(); times = 1; result = new Executable[] { child1, child2, child3 };
				child1.execute(); times =1;
				child2.execute(); times =1;
				child3.execute(); times =1;
			}
		};
		executor.execute(executable);
	}

	@Test
	public void testRunsAllChildrenAfterFailure() throws Exception {
		new NonStrictExpectations() {
			@Mocked Executable child1, child2, child3;
			{
				executable.execute(); times = 1;
					result = null;
					result = new Executable[] { child1, child2, child3 };
				executable.isMissingTags(); result = true;
				child1.execute(); times =1;
				child2.execute(); times =1;
				child3.execute(); times =1;
			}
		};
		executor.execute(executable);
	}
	
	@Test
	public void doesNotFindAllChildrenIfStuck() throws Exception {
		new NonStrictExpectations() {
			@Mocked Executable child1, child2, child3;
			{
				executable.execute(); times = 1;
					result = null;
					result = new Executable[] { child1, child2, child3 };
				executable.isMissingTags(); result = true;
				Executable.allAreStuck((Executable[]) any); result = true;
				child1.execute(); times =0;
				child2.execute(); times =0;
				child3.execute(); times =0;
			}
		};
		executor.execute(executable);
	}
}
