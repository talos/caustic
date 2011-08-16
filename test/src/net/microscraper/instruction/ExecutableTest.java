package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.database.Database;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.test.TestUtils;
import static net.microscraper.test.TestUtils.*;
import net.microscraper.util.BasicVariables;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class ExecutableTest {
	@Mocked private Instruction instruction;
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private Database database;
	//@Mocked private Variables variables;
	private final Variables variables = BasicVariables.empty();
	
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
	public void testFailedBecause(@Mocked final BrowserException exception) throws Exception {
		new NonStrictExpectations() {{
			instruction.generateResults(compiler, browser, exc, null, database); result = exception;
		}};
		exc.run();
		assertTrue(exc.hasFailed());
		assertEquals(exception, exc.failedBecause());
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
	public void testContainsSomeKeysNotOthers() throws Exception {
		final String key = randomString();
		final String notAKey = randomString(key.length() + 1);
		new NonStrictExpectations() {{
			instruction.generateResults(compiler, browser, variables, null, database);
				result = new Result[] { new Result(0, key, randomString())};
		}};
		exc.run();
		assertTrue("Does not contain a key it should.", exc.containsKey(key));
		assertFalse("Contains a key it should not.", exc.containsKey(notAKey));
	}
	
	@Test
	public void testGetDeepChildren(
			@Mocked final Instruction childInstruction,
			@Mocked final Instruction grandchildInstruction) throws Exception {
		final String key = randomString();
		final String value = randomString();
		final String childKey = randomString();
		final String childValue = randomString();
		final String grandchildKey = randomString();
		final String grandchildValue = randomString(); 
		
		final Executable child = new Executable(childInstruction, compiler, browser, variables, null, database);
		final Executable grandchild = new Executable(grandchildInstruction, compiler, browser, variables, null, database);
		new NonStrictExpectations() {
			//Executable child, grandchild;
			{
				onInstance(instruction).generateChildExecutables(compiler, browser, exc, null, database);
					result = new Executable[] { child };
				onInstance(childInstruction).generateChildExecutables(compiler, browser, child, null, database);
					result = new Executable[] { grandchild };
					
				onInstance(instruction).generateResults(compiler, browser, exc, null, database);
					result = new Result[] { new Result(0, key, value) };
				onInstance(childInstruction).generateResults(compiler, browser, exc, null, database);
					result = new Result[] { new Result(0, childKey, childValue) };
				onInstance(grandchildInstruction).generateResults(compiler, browser, child, null, database);
					result = new Result[] { new Result(0, grandchildKey, grandchildValue) };
			}
		};
		
		exc.run();
		child.run();
		grandchild.run();
		
		assertTrue(exc.containsKey(key));
		assertTrue(exc.containsKey(childKey));
		assertTrue(exc.containsKey(grandchildKey));
		assertTrue(child.containsKey(key));
		assertTrue(child.containsKey(childKey));
		assertTrue(child.containsKey(grandchildKey));
		assertTrue(grandchild.containsKey(key));
		assertTrue(grandchild.containsKey(childKey));
		assertTrue(grandchild.containsKey(grandchildKey));
	}

	@Test
	public void testContainsKey() {
		fail("Not yet implemented");
	}

}
