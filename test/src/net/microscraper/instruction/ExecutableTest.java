package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.HashtableDatabase;
import net.microscraper.database.Scope;
import net.microscraper.util.Execution;

import org.junit.Before;
import org.junit.Test;

public class ExecutableTest {
	
	//@Mocked private Database database;
	@Mocked private InstructionPromise promise;
	@Mocked private Instruction instruction;
	@Mocked private Scope scope;
	
	private String source;
	private Executable executable;

	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {
			@Injectable Execution execution;
			{
				promise.load(scope); result = execution;
				onInstance(execution).isSuccessful(); result = true;
				execution.getExecuted(); result = instruction;
			}
		};
		source = randomString();
		executable = new Executable(source, scope, promise);
	}
	
	@Test
	public void testExecuteExecutesInstructionWithVariablesAndSourceOnce() throws Exception {
		new Expectations() {
			{
				instruction.execute(source, scope); times = 1;
			}
		};
		executable.execute();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetLastExecutionThrowsIllegalStateBeforeExecute() {
		executable.getLastExecution();
	}

	@Test()
	public void testGetLastExecutionReturnsLastExecution() throws Exception {
		new NonStrictExpectations() {
			@Injectable Execution execution;
			{
				instruction.execute(source, scope); result = execution;
			}
		};
		Execution execution = executable.execute();
		Execution lastExecution = executable.getLastExecution();
		assertEquals(execution, lastExecution);
	}
	
	@Test
	public void testIsNotStuckBeforeExecute() {
		assertFalse(executable.isStuck());
	}
	
	@Test
	public void testIsNotStuckAfterOneMissingVariables() throws Exception {
		new NonStrictExpectations() {
			@Injectable Execution execution;
			{
				instruction.execute(source, scope); result = execution;
				execution.isMissingVariables(); result = true;
				execution.getMissingVariables(); result = new String[] { randomString() };
			}
		};
		Execution execution = executable.execute();
		assertTrue(execution.isMissingVariables());
		assertFalse(executable.isStuck());
	}

	@Test
	public void testIsNotStuckAfterTwoDifferentMissingVariables() throws Exception {
		new NonStrictExpectations() {
			@Injectable Execution execution1, execution2;
			{
				instruction.execute(source, scope); result = execution1; result = execution2;
				
				execution1.isMissingVariables(); result = true;
				execution1.getMissingVariables(); result = new String[] { randomString(5) };
				
				execution2.isMissingVariables(); result = true;
				execution2.getMissingVariables(); result = new String[] { randomString(4) };
			}
		};
		Execution execution;
		execution = executable.execute();
		assertTrue(execution.isMissingVariables());
		execution = executable.execute();
		assertTrue(execution.isMissingVariables());
		
		assertFalse(executable.isStuck());
	}

	@Test
	public void testIsStuckAfterTwoSameMissingVariables() throws Exception {
		final String[] missingVariables = new String[] { randomString() };
		new NonStrictExpectations() {
			@Injectable Execution execution1, execution2;
			{
				instruction.execute(source, scope); result = execution1; result = execution2;
				
				execution1.isMissingVariables(); result = true;
				execution1.getMissingVariables(); result = missingVariables;
				
				execution2.isMissingVariables(); result = true;
				execution2.getMissingVariables(); result = missingVariables;
			}
		};
		Execution execution;
		execution = executable.execute();
		assertTrue(execution.isMissingVariables());
		execution = executable.execute();
		assertTrue(execution.isMissingVariables());
		
		assertTrue(executable.isStuck());
	}
	
	@Test
	public void testToStringIncludesPromiseToStringAndSourceAndVariablesToString() {
		assertTrue(executable.toString().contains(promise.toString()));
		assertTrue(executable.toString().contains(source.toString()));
	//	assertTrue(executable.toString().contains(variables.toString()));
	}
}
