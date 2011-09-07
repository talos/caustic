package net.microscraper.instruction;

import net.microscraper.database.Scope;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.DependsOnTemplate;
import net.microscraper.template.MissingTags;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.StringUtils;

/**
 * An {@link Executable} for extracting matches from a source string according to
 * a {@link Pattern} and replacement {@link StringTemplate}.
 * @author john
 *
 */
public class Find implements Action {

	/**
	 * The {@link StringTemplate} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final StringTemplate pattern;
	
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
	 * The {@link StringTemplate} that should be substituted evaluated for backreferences,
	 * then returned once for each match, if it is assigned by {@link #setReplacement(StringTemplate)}.
	 */
	private StringTemplate nonDefaultReplacement = null;

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
	
	public Find(RegexpCompiler compiler, StringTemplate pattern) {
		this.compiler = compiler;
		this.pattern = pattern;
	}
	
	public void setReplacement(StringTemplate replacement) {
		this.nonDefaultReplacement = replacement;
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
	 * Use {@link #pattern}, substituted with {@link Variables}, to match against <code>source</code>.
	 */
	public ActionResult execute(String source, Scope scope) {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final ActionResult result;
		StringSubstitution subPattern = pattern.sub(scope);
		
		StringSubstitution subReplacement;
		if(nonDefaultReplacement != null) {
			subReplacement = nonDefaultReplacement.sub(scope);
		} else {
			subReplacement = StringSubstitution.newSuccess(ENTIRE_MATCH);
		}
				
		if(subPattern.isMissingTags() || !subReplacement.isMissingTags()) {
			// One of the substitutions was not OK.
			result = ActionResult.newMissingTags(
					MissingTags.combine( new DependsOnTemplate[] { subPattern, subReplacement } ));

		} else {
			// All the substitutions were OK.
			String patternString = (String) subPattern.getSubstituted();
			Pattern pattern = compiler.compile(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
			
			String replacement = (String) subReplacement.getSubstituted();
			String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
			
			if(matches.length == 0) {
				result = ActionResult.newFailure("Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.truncate(StringUtils.quote(source), 100));
			// We got at least 1 match.
			} else {
				result = ActionResult.newSuccess(matches);
			}
		}
		return result;
	}

	/**
	 * Defaults to {@link #pattern}.
	 */
	public StringTemplate getDefaultName() {
		return pattern;
	}
}
