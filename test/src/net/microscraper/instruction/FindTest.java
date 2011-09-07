package net.microscraper.instruction;

import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Scope;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Execution;

import org.junit.Before;
import org.junit.Test;

public class FindTest  {
	@Mocked RegexpCompiler compiler;
	@Injectable StringTemplate pattern, replacement;
	@Mocked private Scope scope;
	private Find find;
	
	@Before
	public void setUp() throws Exception {
		find = new Find(compiler, pattern);
		find.setReplacement(replacement);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindWithoutSourceThrowsIllegalArgument() throws Exception {
		find.execute(null, scope);
	}
	
	@Test
	public void testMissingVariablesToCompileTemplatePassedUp() throws Exception {
		final String[] missingVariables1 = new String[] { randomString(), randomString() };
		final String[] missingVariables2 = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			@Injectable Execution patternExc, replacementExc;
			{
				pattern.sub(scope); result = patternExc;
				patternExc.isMissingVariables(); result = true;
				patternExc.getMissingVariables(); result = missingVariables1;
				
				replacement.sub(scope); result = replacementExc;
				replacementExc.isMissingVariables(); result = true;
				replacementExc.getMissingVariables(); result = missingVariables2;
		}};
		Execution exc = find.execute(randomString(), scope);
		assertTrue(exc.isMissingVariables());
		List<String> list1 = Arrays.asList(missingVariables1);
		List<String> list2 = Arrays.asList(missingVariables2);
		assertTrue(Arrays.asList(exc.getMissingVariables()).containsAll(list1));
		assertTrue(Arrays.asList(exc.getMissingVariables()).containsAll(list2));
	}

	@Test
	public void testNoMatchesIsFailure() throws Exception {
		final String source = randomString();
		final String patternString = randomString();
		final String replacementString = randomString();
		new NonStrictExpectations() {
			@Injectable Execution patternExc, replacementExc;
			@Injectable Pattern regexpPattern;
			{
				pattern.sub(scope); result = patternExc;
				patternExc.isSuccessful(); result = true;
				patternExc.getExecuted(); result = patternString;
				
				replacement.sub(scope); result = replacementExc;
				replacementExc.isSuccessful(); result = true;
				replacementExc.getExecuted(); result = replacementString;
				
				compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = regexpPattern;
				regexpPattern.match(source, replacementString, anyInt, anyInt); result = new String[] {};
		}};
		Execution exc = find.execute(randomString(), scope);
		assertTrue(exc.hasFailed());
		assertEquals(1, exc.failedBecause().length);
		assertTrue(exc.failedBecause()[0] + " should contain 'match'", exc.failedBecause()[0].contains("match"));
	}
	
}
