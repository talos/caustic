package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonObject;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.RegexpCompiler;
import static net.microscraper.test.TestUtils.*;
import net.microscraper.util.Variables;
import static net.microscraper.instruction.Instruction.*;

import org.junit.Before;
import org.junit.Test;

public final class InstructionTest {	
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private Database database;
	@Mocked private Variables variables;
	
	@Mocked private Find find;
	@Mocked private Load page;
	
	@Mocked private Result source;
	
	private String source = "the quick brown fox";
	private String name = "name";
	private Instruction instruction;
	
	@Before
	public void setUp() throws Exception {
		//instruction = new Instruction(false, MustacheTemplate.compile(name), )
	}
	
	public void testExecuteWithoutSource() throws Exception {
		Result[] results = instruction.execute();
	}
	
	public void testExecuteWithSource() throws Exception {
		Result[] results = instruction.execute(source);
	}
}
