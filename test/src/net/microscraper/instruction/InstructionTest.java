package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.test.TestUtils;
import net.microscraper.util.Variables;
import static net.microscraper.instruction.Instruction.*;

import org.junit.Before;
import org.junit.Test;

public class InstructionTest {	
	@Mocked private JSONObjectInterface jsonObject;
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private Database database;
	@Mocked private Variables variables;
	//@Tested private Instruction instruction;
	//@Mocked private Instruction instruction;
	@Mocked() private Instruction instruction;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDefaultShouldSaveValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testInstruction() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGenerateChildren() {
		fail("Not yet implemented");
	}

	@Test
	public void testRunsAllChildren() throws Exception {
		/*new NonStrictExpectations() {
			JSONArrayInterface children;
			JSONObjectInterface childObject, parentObject;
			{
				onInstance(parentObject).has(FIND); result = true;
				onInstance(parentObject).isJSONArray(FIND); result = true;
				onInstance(parentObject).getJSONArray(FIND); result = children;
				children.length(); result = 3;
				onInstance(childObject).getString(Regexp.PATTERN); result = TestUtils.randomString();
				children.getJSONObject(anyInt); result = childObject;
			}
		};//instruction.generateChildExecutables.generateChildExecutables(compiler, browser, parent, sources, database)
		*/
		instruction.execute(compiler, browser, variables, null, database);
		
		new Verifications() {
			Executable exc;
			{
				exc.run(); times = 4;
		}};
	}

	@Test
	public void testGenerateResultValues() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDefaultName() {
		fail("Not yet implemented");
	}

}
