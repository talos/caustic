package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.io.IOException;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.database.Database;
import static net.microscraper.util.TestUtils.*;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public final class InstructionTest {	
	@Mocked private Database database;
	@Mocked private Template name;
	@Mocked private InstructionPromise promise;
	@Mocked private Action action;
	
	private Variables variables;
	private String source = randomString();
	
	@Tested private Instruction instruction;
	
	@Before
	public void setUp() throws Exception {
		variables = Variables.empty(database);
		instruction = new Instruction(action);
		instruction.setName(name);
	}

	@Test
	public void testPassesNameMissingVariables() throws Exception {
		final String[] missingVariableNames = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			Execution execution;
			{
				name.sub(variables); result = execution;
				execution.isMissingVariables(); result = true;
				execution.getMissingVariables(); result = missingVariableNames;
				action.execute(source, variables); times = 0; $ = "Should not execute the action if name is missing variables.";
			}
		};
		
		Execution exc = instruction.execute(source, variables);
		assertTrue(exc.isMissingVariables());
		assertArrayEquals(missingVariableNames, exc.getMissingVariables());
	}
	
	@Test
	public void testPassesActionMissingVariables() throws Exception {
		final String[] missingVariableNames = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			Execution nameExecution, actionExecution;
			{
				name.sub(variables); result = nameExecution;
				nameExecution.isSuccessful(); result = true;
				nameExecution.getExecuted(); result = randomString();
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isMissingVariables(); result = true;
				actionExecution.getMissingVariables(); result = missingVariableNames;
			}
		};
		
		Execution exc = instruction.execute(source, variables);
		assertTrue(exc.isMissingVariables());
		assertArrayEquals(missingVariableNames, exc.getMissingVariables());
	}

	@Test
	public void testProducesExecutableArrayProductOfInstructionAndResults() throws Exception {
		final String[] actionResults = new String[] { randomString(), randomString(), randomString() };
		final InstructionPromise[] children = new InstructionPromise[] { promise, promise, promise, promise };
		
		new NonStrictExpectations() {
			@Injectable Execution nameExecution, actionExecution;
			{
				name.sub(variables); result = nameExecution;
				nameExecution.isSuccessful(); result = true;
				nameExecution.getExecuted(); result = randomString();
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isSuccessful(); result = true;
				actionExecution.getExecuted(); result = actionResults;
			}
		};
		
		for(int i = 0 ; i < children.length ; i ++) {
			instruction.addChild(children[i]);
		}
		Execution exc = instruction.execute(source, variables);
		
		assertTrue(exc.isSuccessful());
		assertTrue(exc.getExecuted() instanceof Executable[]);
		assertEquals(actionResults.length * children.length, ((Executable[]) exc.getExecuted()).length);
	}
	
	@Test(expected = IOException.class)
	public void testThrowsIOExceptionOnPersistError() throws Exception {
		final String[] actionResults = new String[] { randomString(), randomString(), randomString() };
		
		new NonStrictExpectations() {
			@Injectable Execution nameExecution, actionExecution;
			{
				name.sub(variables); result = nameExecution;
				nameExecution.isSuccessful(); result = true;
				nameExecution.getExecuted(); result = randomString();
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isSuccessful(); result = true;
				actionExecution.getExecuted(); result = actionResults;
				database.storeInitial(anyString, anyString, anyInt); result = new IOException();
			}
		};
		
		instruction.execute(source, variables);
	}
}
