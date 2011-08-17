package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.io.IOException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.json.JsonObject;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.test.TestUtils;
import static net.microscraper.test.TestUtils.*;
import net.microscraper.util.BasicVariables;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class ExecutionTest {
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private Database database;
	//@Mocked private Variables variables;
	private final Variables variables = BasicVariables.empty();
	private Execution exc;
	
	@Before
	public void setUp() throws Exception {
		exc = new Execution(instruction, compiler, browser, variables, null, database);
	}
	
	@Test
	public void testIsStuckBecauseOfMissingVariableOfSameNameTwice() throws Exception {				
		exc.run();
		assertEquals("Should not be stuck after the first run.", false, exc.isStuck());
		exc.run();
		assertEquals("Should be stuck after the second run.", true, exc.isStuck());
	}

	@Test
	public void testIsNotStuckBecauseOfTwoDifferentMissingVariables() throws Exception {
		
		exc.run();
		exc.run();
		assertEquals(false, exc.isStuck());
	}
	
	@Test
	public void testIsUnstuckAfterMissingADifferentVariable() throws Exception {
		
		exc.run();
		exc.run();
		assertEquals(true, exc.isStuck());
		exc.run();
		assertEquals(false, exc.isStuck());
	}
	
	@Test(expected = Exception.class)
	public void testHasFailedIsNotCatchAll() throws Exception {

		exc.run();
	}

	@Test
	public void testFailedBecause(@Mocked final BrowserException exception) throws Exception {
		exc.run();
		assertTrue(exc.hasFailed());
		assertEquals(exception, exc.failedBecause());
	}

	@Test
	public void testIsNotCompleteUntilChildrenAndResultsAreFilled() throws Exception {
		
		exc.run();
		assertFalse("Execution should not be complete until children are generated.", exc.isComplete());
		exc.run();
		assertTrue("Execution should be complete once children are generated.", exc.isComplete());
	}

	@Test
	public void testExecutableToStringIsExecutableToString() {
		assertEquals(instruction.toString(), exc.toString());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetChildrenThrowsIllegalStateBeforeComplete() {
		exc.getChildren();
	}
	
	@Test
	public void testExecutesExecutableOnlyOnce() throws Exception {
		for(int i = 0 ; i < 10 ; i ++) {
			exc.run();
		}
	}

	@Test
	public void testExecutesExecutableUntilFailure() throws Exception {
		for(int i = 0 ; i < 10 ; i ++) {
			exc.run();
		}
	}
		
	@Test
	public void testContainsSomeKeysNotOthers() throws Exception {
		final String key = randomString();
		final String notAKey = randomString(key.length() + 1);

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
