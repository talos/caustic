package net.caustic.executor;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.caustic.database.Database;
import net.caustic.database.DatabaseView;
import net.caustic.executor.Executable;
import net.caustic.executor.SyncExecutor;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;

import org.junit.Before;
import org.junit.Test;

public class SyncExecutorTest {
	private @Mocked Instruction instruction;
	private @Mocked Database db;
	private @Mocked String name, source, source2;
	private @Mocked HttpBrowser browser;
	
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
		new Expectations() {
			DatabaseView dbView;
			{
			db.newView(); result = dbView;
			instruction.execute(source, dbView, browser); times = 1;
		}};
		executor.execute(instruction, db, source, browser);
	}

	@Test
	public void testRunsAllChildren() throws Exception {
		new NonStrictExpectations() {
			@Mocked Instruction child1, child2, child3;
			DatabaseView dbView;
			{
				db.newView(); result = dbView;
				instruction.execute(source, dbView, browser); times = 1;
						result = InstructionResult.success(name,
								new String[] { source2 },
								new Instruction[]{ child1, child2, child3 }, false);
				child1.execute(source2, (DatabaseView) any, (HttpBrowser) browser); times =1;
				child2.execute(source2, (DatabaseView) any, (HttpBrowser) browser); times =1;
				child3.execute(source2, (DatabaseView) any, (HttpBrowser) browser); times =1;
			}
		};
		executor.execute(instruction, db, source, browser);
	}
/*
	@Test
	public void testRunsAllChildrenAfterFailure() throws Exception {
		new NonStrictExpectations() {
			@Mocked Instruction child1, child2, child3;
			{
				instruction.execute(source, view, browser); times = 1;
					result = InstructionResult.missingTags(missingTags);
					result = InstructionResult.success(name,
						new String[] { source2 },
						new Instruction[]{ child1, child2, child3 }, false);
				executable.isMissingTags(); result = true;
				child1.execute(); times =1;
				child2.execute(); times =1;
				child3.execute(); times =1;
			}
		};
		executor.execute(instruction, view, source, browser);
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
	}*/
}
