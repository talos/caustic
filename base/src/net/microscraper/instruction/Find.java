package net.microscraper.instruction;

import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.Template;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for extracting matches from a source string according to
 * a {@link Pattern} and replacement {@link Template}.
 * @author john
 *
 */
public class Find implements Action {

	/**
	 * The {@link Template} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final Template pattern;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#CASE_INSENSITIVE}
	 */
	private boolean isCaseInsensitive = false;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#MULTILINE}
	 */
	private boolean isMultiline = false;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#DOTALL}
	 */
	private boolean doesDotMatchNewline = true;
	
	/**
	 * The {@link RegexpCompiler} to use when compiling this {@link Find}.
	 */
	private final RegexpCompiler compiler;
	
	/**
	 * The {@link String} that should be templated and evaluated for backreferences,
	 * then returned once for each match.<p>
	 * Defaults to {@link #ENTIRE_MATCH}.
	 */
	private Template replacement;

	/**
	 * {@link PatternTemplate}s that test the sanity of the parser's output.
	 */
	//private final Vector tests = new Vector();

	/**
	 * Value for when {@link #replacement} is the entire match.
	 */
	public static final String ENTIRE_MATCH = "$0";
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * <p>
	 * @see #maxMatch
	 */
	private int minMatch = Pattern.FIRST_MATCH;

	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * <p>
	 * @see #minMatch
	 */
	private int maxMatch = Pattern.LAST_MATCH;
	
	public Find(RegexpCompiler compiler, Template pattern) {
		this.compiler = compiler;
		this.pattern = pattern;
		try {
			this.replacement = Template.compile(ENTIRE_MATCH,
					Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		} catch(TemplateCompilationException e) {
			throw new RuntimeException(e); // this shouldn't happen
		}
	}

	public void setReplacement(Template replacement) {
		this.replacement = replacement;
	}
	
	public void setMinMatch(int min) {
		this.minMatch = min;
	}
	
	public void setMaxMatch(int max) {
		this.maxMatch = max;
	}
	
	public void setIsCaseInsensitive(boolean isCaseInsensitive) {
		this.isCaseInsensitive = isCaseInsensitive;
	}
	
	public void setDoesDotMatchAll(boolean doesDotMatchAll) {
		this.doesDotMatchNewline = doesDotMatchAll;
	}
	
	public void setIsMultiline(boolean isMultiline) {
		this.isMultiline = isMultiline;
	}
	
	/**
	 * Add a {@link Find} that will be used to test the substitutions.
	 * {@link #execute(String, Variables)} will return a {@link Execution#failedTests(String, Pattern)}
	 * if one of these fails to {@link Pattern#matches(String, int)}.
	 * @param test The {@link PatternTemplate} to add as a test.
	 */
	/*public void addTest(Find test) {
		tests.add(test);
	}*/
	
	/**
	 * Use {@link #pattern}, substituted with {@link Variables}, to match against <code>source</code>.
	 * Will return {@link Execution#failedTests(String, Pattern)} if at least one of the {@link #tests}
	 * does not match against at least one of the result {@link String}s.
	 */
	public Execution execute(String source, Variables variables) {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final Execution result;
		Execution subPattern = pattern.sub(variables);
		Execution subReplacement = replacement.sub(variables);
				
		if(!subPattern.isSuccessful() || !subReplacement.isSuccessful()) {
			// One of the substitutions was not OK.
			result = Execution.combine( new Execution[] { subPattern, subReplacement } );

		} else {
			// All the substitutions were OK.
			String patternString = (String) subPattern.getExecuted();
			Pattern pattern = compiler.compile(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
			
			String replacement = (String) subReplacement.getExecuted();
			String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
			
			if(matches.length == 0) {
				result = Execution.noMatches(source, pattern, minMatch, maxMatch);
			// We got at least 1 match.
			} else {
				// Run the tests.
				/*Vector failedExecutions = new Vector();
				for(int i = 0 ; i < tests.length ; i ++) {
					Pattern test = (Pattern) tests[i];
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
				}*/
				result = Execution.success(matches);
			}
		}
		return result;
	}
	
	/**
	 * {@link Find}'s default name is its {@link #pattern}.
	 */
	public Template getDefaultName() {
		return pattern;
	}

	/**
	 * {@link Find} does persist its value by default.
	 */
	public boolean getDefaultShouldPersistValue() {
		return true;
	}
}
