package net.microscraper.instruction;

import java.util.Vector;

import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for extracting matches from a source string according to
 * a {@link Pattern} and substitution.
 * @author john
 *
 */
public class Find implements Action {

	/**
	 * The {@link String} that should be mustached and evaluated for backreferences,
	 * then returned once for each match.<p>
	 * Defaults to {@link #ENTIRE_MATCH}.
	 */
	private final MustacheTemplate replacement;

	/**
	 * {@link MustachePattern}s that test the sanity of the parser's output.
	 */
	private final MustachePattern[] tests;

	/**
	 * Value for when {@link #replacement} is the entire match.
	 */
	public static final String ENTIRE_MATCH = "$0";

	/**
	 * 
	 * The {@link MustachePattern} inside this {@link Find}.
	 */
	private final MustachePattern pattern;
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * <p>
	 * @see #maxMatch
	 */
	private final int minMatch;

	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * <p>
	 * @see #minMatch
	 */
	private final int maxMatch;
	
	public Find(MustachePattern pattern, MustacheTemplate replacement,
			int minMatch, int maxMatch, MustachePattern[] tests) {
		this.pattern = pattern;
		this.replacement = replacement;
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
		this.tests = tests;
	}
	
	public Execution execute(String source, Variables variables) {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final Execution result;
		Execution subPattern = pattern.sub(variables);
		Execution subReplacement = replacement.sub(variables);
		Execution subTests = Execution.arraySub(tests, variables);
		
		if(!subPattern.isSuccessful() || !subReplacement.isSuccessful() || !subTests.isSuccessful()) {
			// One of the substitutions was not OK.
			result = Execution.missingVariables(Execution.combine(
					new Execution[] { subPattern, subReplacement, subTests }
			).getMissingVariables());

		} else {
			// All the substitutions were OK.
			Pattern pattern = (Pattern) subPattern.getExecuted();
			String replacement = (String) subReplacement.getExecuted();
			String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
			Pattern[] tests = (Pattern[]) subTests.getExecuted();
			
			// We got at least 1 match.
			if(matches.length == 0) {
				result = Execution.noMatches(source, pattern, minMatch, maxMatch);
			} else {
				// Run the tests.
				Vector failedExecutions = new Vector();
				for(int i = 0 ; i < tests.length ; i ++) {
					Pattern test = tests[i];
					for(int j = 0 ; j < matches.length ; j ++) {
						String tested = matches[j];
						boolean passed = test.matches(tested, Pattern.FIRST_MATCH);
						if(passed == false) {
							failedExecutions.add(Execution.failedTests(tested, test));
						}
					}
				}
				
				// Failed a test :(
				if(failedExecutions.size() > 0) {
					Execution[] failedExecutionsAry = new Execution[failedExecutions.size()];
					failedExecutions.copyInto(failedExecutionsAry);
					//result = Execution.failedTests(matches[j], failedTestsAry);
					result = Execution.combine(failedExecutionsAry);
				} else { // Passed all tests! :)
					result = Execution.success(matches);
				}
			}
		}
		return result;
	}
	
	/**
	 * {@link Find}'s default name is its {@link #pattern}'s {@link MustachePattern#getTemplate()}.
	 */
	public MustacheTemplate getDefaultName() {
		return pattern.getTemplate();
	}

	/**
	 * {@link Find} does persist its value by default.
	 */
	public boolean getDefaultShouldPersistValue() {
		return true;
	}
}
