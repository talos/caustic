package net.caustic.instruction;

import static net.caustic.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.caustic.database.DatabaseView;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.InstructionResult;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.StringSubstitution;

import org.junit.Before;
import org.junit.Test;

public class FindTest  {
	@Mocked RegexpCompiler compiler;
	@Mocked DatabaseView input;
	@Mocked HttpBrowser browser;
	@Injectable StringTemplate pattern, replacement;
	private Find find;
	
	@Before
	public void setUp() throws Exception {
		find = new Find(compiler, pattern);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindWithoutSourceThrowsIllegalArgument() throws Exception {
		find.execute(null, input, browser);
	}
	
	@Test
	public void testMissingVariablesToCompileTemplatePassedUp() throws Exception {
		final String[] missingVariables1 = new String[] { randomString(), randomString() };
		final String[] missingVariables2 = new String[] { randomString(), randomString() };
		
		new NonStrictExpectations() {
			@Injectable StringSubstitution patternSub, replaceSub;
			{
				pattern.sub(input); result = patternSub;
				replacement.sub(input); result = replaceSub;
				
				patternSub.isMissingTags(); result = true;
				replaceSub.isMissingTags(); result = true;
				
				replaceSub.getMissingTags(); result = missingVariables2;
				patternSub.getMissingTags(); result = missingVariables1;
		}};
		find.setReplacement(replacement);
		InstructionResult result = find.execute(randomString(), input, browser);
		
		assertTrue(result.isMissingTags());
		List<String> list1 = Arrays.asList(missingVariables1);
		List<String> list2 = Arrays.asList(missingVariables2);
		assertTrue(Arrays.asList(result.getMissingTags()).containsAll(list1));
		assertTrue(Arrays.asList(result.getMissingTags()).containsAll(list2));
	}
	
	@Test
	public void testNoMatchesIsFailure() throws Exception {
		final String source = randomString();
		final String patternString = randomString();
		final String replacementString = randomString();
		new NonStrictExpectations() {
			@Injectable StringSubstitution patternSub, replaceSub;
			@Injectable Pattern regexpPattern;
			{
				pattern.sub(input); result = patternSub;
				patternSub.getSubstituted(); result = patternString;
				
				replacement.sub(input); result = replaceSub;
				replaceSub.getSubstituted(); result = replacementString;
				
				compiler.newPattern(patternString, anyBoolean, anyBoolean, anyBoolean); result = regexpPattern;
				regexpPattern.match(source, replacementString, anyInt, anyInt); result = new String[] {};
		}};
		find.setReplacement(replacement);
		InstructionResult result = find.execute(randomString(), input, browser);
		
		assertFalse(result.isMissingTags());
		assertTrue(result.getFailedBecause() + " should contain 'match'", result.getFailedBecause().contains("match"));
	}
}
