package net.caustic.instruction;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.template.DependsOnTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.StaticStringTemplate;
import net.caustic.util.StringUtils;

public final class Find extends Instruction {
	
	/**
	 * The {@link StringTemplate} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final StringTemplate pattern;
	
	private final Instruction[] children;
	
	private final boolean hasName;
	
	private final RegexpCompiler compiler;
	
	private final StringTemplate name;
	
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
	 * Value for when {@link #replacement} is the entire match.
	 */
	public static final String ENTIRE_MATCH = "$0";

	/**
	 * The {@link StringTemplate} that should be substituted evaluated for backreferences,
	 * then returned once for each match, if it is assigned by {@link #setReplacement(StringTemplate)}.
	 */
	private StringTemplate replacement = new StaticStringTemplate(ENTIRE_MATCH);
	
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
	
	public Find(RegexpCompiler compiler, StringTemplate pattern, Instruction[] children) {
		this.hasName = false;
		this.name = pattern;
		this.compiler = compiler;
		this.pattern = pattern;
		this.children = children;
	}
	
	public Find(StringTemplate name, RegexpCompiler compiler, StringTemplate pattern,
			Instruction[] children) {
		this.hasName = true;
		this.name = name;
		this.compiler = compiler;
		this.pattern = pattern;
		this.children = children;
	}
	
	/*
	public void setName(StringTemplate name) {
		this.hasName = true;
		this.name = name;
	}
	*/
	
	public void setReplacement(StringTemplate replacement) {
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
	 * @return The raw pattern template.
	 */
	public String toString() {
		return pattern.toString();
	}

	public boolean shouldConfirm() {
		return false;
	}
	
	/**
	 * Use {@link #pattern}, substituted from {@link Database}, to match against <code>source</code>.
	 * Ignores <code>browser</code>.
	 */
	public void execute(String source, Database db, Scope scope, HttpBrowser browser)
			throws DatabaseException {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final StringSubstitution subName;
		final StringSubstitution subPattern = pattern.sub(db, scope);
		final StringSubstitution subReplacement = replacement.sub(db, scope);
		
		subName = name.sub(db, scope);
		
		if(subName.isMissingTags() ||
				subPattern.isMissingTags() ||
				subReplacement.isMissingTags()) { // One of the substitutions was not OK.
			final String[] missingTags = StringSubstitution.combine(
					new DependsOnTemplate[] { subName, subPattern, subReplacement });
			db.putMissing(scope, source, this, missingTags);
			return;
		}
		
		// All the substitutions were OK.
		String resultName = subName.getSubstituted();
		
		String patternString = (String) subPattern.getSubstituted();
		Pattern pattern = compiler.newPattern(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
		
		String replacement = (String) subReplacement.getSubstituted();
		String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
		
		if(matches.length == 0) { // No matches, fail out.
			db.putFailed(scope, source, this, "Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.quoteAndTruncate(StringUtils.quote(source), 100));
			return;
		}
		
		// We got at least 1 match.
		for(int i = 0 ; i < matches.length ; i ++) {
			
			final Scope childScope;
			// generate result scopes.
			final String childSource = matches[i];
			
			if(matches.length == 1) { // don't spawn a new result for single match
				childScope = scope;
				if(hasName) {
					db.put(childScope, resultName, childSource);
				}
			} else {
				if(hasName) {
					childScope = db.newScope(scope, resultName, childSource);
				} else {
					childScope = db.newScope(scope, resultName); // use auto-name			
				}
			}
			
			// Insert children.
			for(int j = 0 ; j < children.length ; j ++) {
				db.putReady(childScope, childSource, children[j]);
			}
		}
		
		db.putSuccess(scope, source, this);
	}
}
