package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.io.IOException;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.database.Database;
import net.microscraper.database.Variables;
import static net.microscraper.util.TestUtils.*;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;

import org.junit.Before;
import org.junit.Test;

public final class InstructionTest {	
	@Mocked private Database database;
	@Mocked private InstructionPromise promise;
	@Mocked private Action action;
	
	private Variables variables;
	private String source;
	private int firstId;
	private Template defaultName;
	
	@Tested private Instruction instruction;
	
	@Before
	public void setUp() throws Exception {
		firstId = randomInt();
		source = randomString();
		defaultName = Template.compile(randomString(), Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		variables = new Variables(database, firstId);
		instruction = new Instruction(action);
		instruction.setName(defaultName);
	}

	@Test
	public void testPassesNameMissingVariables() throws Exception {
		new Expectations() {{
			action.execute(source, variables); times = 0; $ = "Should not execute the action if name is missing variables.";
		}};
		instruction.setName(Template.compile("{{requires}} {{variables}}", "{{", "}}"));
		
		Execution exc = instruction.execute(source, variables);
		assertTrue(exc.isMissingVariables());
		assertArrayEquals(new String[] { "requires", "variables" }, exc.getMissingVariables());
	}
	
	@Test
	public void testPassesActionMissingVariables() throws Exception {
		final String[] missingVariableNames = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			@Injectable Execution actionExecution;
			{
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
			@Injectable Execution actionExecution;
			{
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

	@Test()
	public void testSavesToDatabaseWhenNoNameDefined() throws Exception {
		final String[] actionResults = new String[] { randomString(), randomString(), randomString() };
		new Expectations() {
			@Injectable Execution actionExecution;
			{
				action.getDefaultName(); result = defaultName;
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isSuccessful(); result = true;
				actionExecution.getExecuted(); result = actionResults;
				database.storeOneToMany(firstId, defaultName.toString()); times = 3;
			}
		};
		instruction = new Instruction(action);

		instruction.execute(source, variables);
	}
	
	@Test()
	public void testSavesToDatabaseWhenNameDefined() throws Exception {
		final String[] actionResults = new String[] { randomString(), randomString(), randomString() };
		final String nameStr = randomString();
		new Expectations() {
			@Injectable Execution actionExecution;
			{
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isSuccessful(); result = true;
				actionExecution.getExecuted(); result = actionResults;
				database.storeOneToMany(firstId, nameStr, actionResults[0]);
				database.storeOneToMany(firstId, nameStr, actionResults[1]);
				database.storeOneToMany(firstId, nameStr, actionResults[2]);
			}
		};
		instruction.setName(Template.compile(nameStr, "{{", "}}"));
		instruction.execute(source, variables);
	}
	
	@Test(expected = IOException.class)
	public void testThrowsIOExceptionOnPersistError() throws Exception {
		final String[] actionResults = new String[] { randomString(), randomString(), randomString() };
		new Expectations() {
			@Injectable Execution actionExecution;
			{
				action.execute(source, variables); times = 1; result = actionExecution;
				actionExecution.isSuccessful(); result = true;
				actionExecution.getExecuted(); result = actionResults;
				database.storeOneToMany(firstId, anyString, anyString); result = new IOException();
			}
		};
		instruction.setName(Template.compile(randomString(), "{{", "}}"));
		instruction.execute(source, variables);
	}
}
