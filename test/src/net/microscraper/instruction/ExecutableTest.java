package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.test.TestUtils;
import static net.microscraper.test.TestUtils.*;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class ExecutableTest {
	@Mocked private Instruction instruction;
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private Database database;
	@Mocked private Variables variables;
	
	private Executable exc;
	
	@Before
	public void setUp() throws Exception {
		exc = new Executable(instruction, compiler, browser, variables, null, database);
	}
	
	@Test
	public void testIsStuckBecauseOfMissingVariableOfSameNameTwice() throws Exception {		
		new NonStrictExpectations() {
			MissingVariableException missingVariable;
			{
				missingVariable.getName(); result = TestUtils.randomString();
				instruction.generateResults(compiler, browser, exc, null, database); result = missingVariable;
			}
		};
		
		exc.run();
		assertEquals("Should not be stuck after the first run.", false, exc.isStuck());
		exc.run();
		assertEquals("Should be stuck after the second run.", true, exc.isStuck());
	}

	@Test
	public void testIsNotStuckBecauseOfTwoDifferentMissingVariables() throws Exception {
		new NonStrictExpectations() {
			MissingVariableException missingVariable1, missingVariable2;
			{
				onInstance(missingVariable1).getName(); result = TestUtils.randomString(10);
				onInstance(missingVariable2).getName(); result = TestUtils.randomString(11);
				instruction.generateResults(compiler, browser, exc, null, database); result = missingVariable1; result = missingVariable2;
			}
		};
		
		exc.run();
		exc.run();
		assertEquals(false, exc.isStuck());
	}
	
	@Test
	public void testIsUnstuckAfterMissingADifferentVariable() throws Exception {
		new NonStrictExpectations() {
			MissingVariableException missingVariable1, missingVariable2;
			{
				onInstance(missingVariable1).getName(); result = TestUtils.randomString(10);
				onInstance(missingVariable2).getName(); result = TestUtils.randomString(11);
				instruction.generateResults(compiler, browser, exc, null, database); result = missingVariable1; result = missingVariable1; result = missingVariable2;
			}
		};
		
		exc.run();
		exc.run();
		assertEquals(true, exc.isStuck());
		exc.run();
		assertEquals(false, exc.isStuck());
	}
	
	@Test(expected = Exception.class)
	public void testHasFailedIsNotCatchAll() throws Exception {
		new NonStrictExpectations() {
			Exception exception;
			{
				instruction.generateResults(compiler, browser, exc, null, database); result = exception;
			}
		};
		
		exc.run();
	}

	@Test
	public void testFailedBecause() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsNotCompleteUntilChildrenAndResultsAreFilled() throws Exception {
		new NonStrictExpectations() {
			MissingVariableException missingVariable;
			{
				instruction.generateChildExecutables(compiler, browser, exc, (Result[]) any, database);
						result = missingVariable; result = new Executable[] {};
			}
		};
		
		exc.run();
		assertFalse("Execution should not be complete until children are generated.", exc.isComplete());
		exc.run();
		assertTrue("Execution should be complete once children are generated.", exc.isComplete());
	}

	@Test
	public void testExecutableToStringIsInstructionToString() {
		new NonStrictExpectations() {{
			instruction.toString(); result = TestUtils.randomString();
		}};
		assertEquals(instruction.toString(), exc.toString());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetChildrenThrowsIllegalStateBeforeComplete() {
		exc.getChildren();
	}
	
	@Test
	public void testExecutesInstructionOnlyOnce() throws Exception {
		new NonStrictExpectations() {{
			instruction.generateResults(compiler, browser, exc, null, database); times = 1;
		}};
		for(int i = 0 ; i < 10 ; i ++) {
			exc.run();
		}
	}

	@Test
	public void testExecutesInstructionUntilFailure() throws Exception {
		new NonStrictExpectations() {{
			instruction.generateResults(compiler, browser, exc, null, database); times = 1;
		}};
		for(int i = 0 ; i < 10 ; i ++) {
			exc.run();
		}
	}
	
	@Test
	public void testGeneratesChildExecutablesOnlyOnce() throws Exception {
		new NonStrictExpectations() {{
			instruction.generateChildExecutables(compiler, browser, exc, (Result[]) any, database); times = 1;
		}};
		for(int i = 0 ; i < 10 ; i ++) {
			exc.run();
		}
	}
	
	@Test
	public void testGetDeepChildren() throws Exception {
		final String childKey = randomString();
		final String childValue = randomString();
		final String grandchildKey = randomString();
		final String grandchildValue = randomString(); 
		new NonStrictExpectations() {
			Executable child, grandchild;
			{
				instruction.generateChildExecutables(compiler, browser, exc, null, database);
			}
		};
		
		exc.run();
	}

	@Test
	public void testContainsKey() {
		fail("Not yet implemented");
	}

}
