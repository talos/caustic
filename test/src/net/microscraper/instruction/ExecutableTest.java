package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class ExecutableTest {
	
	@Mocked private Variables variables;
	@Mocked private Instruction instruction;
	
	private String source;
	private Executable executable;

	@Before
	public void setUp() throws Exception {
		source = randomString();
		executable = new Executable(source, variables, instruction);
	}
	
	@Test
	public void testExecuteExecutesInstructionWithVariablesAndSourceOnce() throws Exception {
		new Expectations() {{
			instruction.execute(source, variables); times = 1;
		}};
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
				instruction.execute(source, variables); result = execution;
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
				instruction.execute(source, variables); result = execution;
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
				instruction.execute(source, variables); result = execution1; result = execution2;
				
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
				instruction.execute(source, variables); result = execution1; result = execution2;
				
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
	public void testToStringIncludesInstructionToStringAndSourceAndVariablesToString() {
		assertTrue(executable.toString().contains(instruction.toString()));
		assertTrue(executable.toString().contains(source.toString()));
		assertTrue(executable.toString().contains(variables.toString()));
	}
}