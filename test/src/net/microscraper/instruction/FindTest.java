package net.microscraper.instruction;

import static net.microscraper.instruction.Find.*;
import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.database.Database;
import net.microscraper.json.JsonObject;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.PatternTemplate;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class FindTest  {
	@Mocked PatternTemplate pattern;
	@Mocked Database database;
	@Injectable Template replacement;
	private Variables variables;
	private Find find;
	
	@Before
	public void setUp() throws Exception {
		variables = Variables.empty(database);
		find = new Find(pattern, replacement, 0, -1, new PatternTemplate[] {});
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindWithoutSourceThrowsIllegalArgument() throws Exception {
		find.execute(null, variables);
	}
	
	@Test
	public void testMissingVariablesToCompileTemplatePassedUp() throws Exception {
		final String[] missingVariables1 = new String[] { randomString(), randomString() };
		final String[] missingVariables2 = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			@Injectable Execution patternExc, replacementExc;
			{
				pattern.sub(variables); result = patternExc;
				patternExc.isMissingVariables(); result = true;
				patternExc.getMissingVariables(); result = missingVariables1;
				
				replacement.sub(variables); result = replacementExc;
				replacementExc.isMissingVariables(); result = true;
				replacementExc.getMissingVariables(); result = missingVariables2;
		}};
		Execution exc = find.execute(randomString(), variables);
		assertTrue(exc.isMissingVariables());
		List<String> list1 = Arrays.asList(missingVariables1);
		List<String> list2 = Arrays.asList(missingVariables2);
		assertTrue(Arrays.asList(exc.getMissingVariables()).containsAll(list1));
		assertTrue(Arrays.asList(exc.getMissingVariables()).containsAll(list2));
	}

	@Test
	public void testNoMatchesIsFailure() throws Exception {
		final String source = randomString();
		final String replacementString = randomString();
		new NonStrictExpectations() {
			@Injectable Execution patternExc, replacementExc;
			@Injectable Pattern regexpPattern;
			{
				pattern.sub(variables); result = patternExc;
				patternExc.isSuccessful(); result = true;
				patternExc.getExecuted(); result = regexpPattern;
				
				replacement.sub(variables); result = replacementExc;
				replacementExc.isSuccessful(); result = true;
				replacementExc.getExecuted(); result = randomString();
				
				regexpPattern.match(source, replacementString, anyInt, anyInt); result = new String[] {};
		}};
		Execution exc = find.execute(randomString(), variables);
		assertTrue(exc.hasFailed());
		assertEquals(1, exc.failedBecause().length);
		assertTrue(exc.failedBecause()[0].contains("No matches"));
	}
	
}
