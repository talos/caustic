package net.microscraper.instruction;

import java.util.Vector;

import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.Substitution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for extracting matches from a source string according to
 * a {@link Pattern} and substitution.
 * @author john
 *
 */
public class Find implements Executable {

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
		final Execution result;
		Substitution subPattern = pattern.sub(variables);
		Substitution subReplacement = replacement.sub(variables);
		Substitution subTests = Substitution.arraySub(tests, variables);
		
		if(!subPattern.isSuccessful() || !subReplacement.isSuccessful() || !subTests.isSuccessful()) {
			// One of the substitutions was not OK.
			result = Execution.missingVariables(Substitution.combine(
					new Substitution[] { subPattern, subReplacement, subTests }
			).getMissingVariables());

		} else {
			// All the substitutions were OK.
			Pattern pattern = (Pattern) subPattern.getSubstituted();
			String replacement = (String) subReplacement.getSubstituted();
			String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
			Pattern[] tests = (Pattern[]) subTests.getSubstituted();
			
			// We got at least 1 match.
			if(matches.length == 0) {
				result = Execution.noMatches();
			} else {
				// Run the tests.
				Vector failedTests = new Vector();
				for(int i = 0 ; i < tests.length ; i ++) {
					for(int j = 0 ; j < matches.length ; j ++) {
						boolean passed = tests[i].matches(matches[j], Pattern.FIRST_MATCH);
						if(passed == false) {
							failedTests.add(tests[i]);
						}
					}
				}
				
				// Failed a test :(
				if(failedTests.size() > 0) {
					Pattern[] failedTestsAry = new Pattern[failedTests.size()];
					failedTests.copyInto(failedTestsAry);
					result = Execution.failedTests(failedTestsAry);
				} else { // Passed all tests! :)
					result = Execution.success(matches);
				}
			}
		}
		return result;
	}
}
