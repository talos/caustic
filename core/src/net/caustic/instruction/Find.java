package net.caustic.instruction;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
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
	 * Key for deserializing {@link PatternTemplate#pattern}.
	 */
	public static final String FIND = "find";

	
	/**
	 * Key for {@link Instruction#name} value when deserializing {@link Instruction} from JSON.
	 */
	public static final String NAME = "name";
	

	/**
	 * Conveniently deserialize {@link Find#minMatch} and {@link Find#maxMatch}.
	 * If this exists in an object, both {@link #maxMatch} and {@link #minMatch} are its
	 * value.<p>
	 */
	public static final String MATCH = "match";

	/**
	 * Key for {@link Find#minMatch} value when deserializing from JSON.
	 */
	public static final String MIN_MATCH = "min";

	/**
	 * Key for {@link Find#maxMatch} value when deserializing from JSON.
	 */
	public static final String MAX_MATCH = "max";
	
	/**
	 * Key for {@link Find#replacement} value deserializing from JSON.
	 */
	public static final String REPLACE = "replace";
	
	public static final int MAX_MATCH_DEFAULT = -1;
	
	public static final int MIN_MATCH_DEFAULT = 0;
	
	public static final StringTemplate REPLACE_DEFAULT = new StaticStringTemplate("$0");
	
	/**
	 * Key for deserializing {@link PatternTemplate#isCaseInsensitive}.
	 */
	public static final String IS_CASE_INSENSITIVE = "case_insensitive";
	public static final boolean IS_CASE_INSENSITIVE_DEFAULT = false;
	
	/**
	 * Key for deserializing {@link PatternTemplate#isMultiline}.
	 */
	public static final String IS_MULTILINE = "multiline";
	public static final boolean IS_MULTILINE_DEFAULT = false;
	
	/** 
	 * Key for deserializing {@link PatternTemplate#doesDotMatchNewline}.
	 */
	public static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	public static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;

	
	/**
	 * The {@link StringTemplate} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final StringTemplate pattern;
	
	private final String[] children;
	
	private final boolean hasName;
	
	private final RegexpCompiler compiler;
	
	private final StringTemplate name;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#CASE_INSENSITIVE}
	 */
	private final boolean isCaseInsensitive;// = false;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#MULTILINE}
	 */
	private final boolean isMultiline;// = false;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#DOTALL}
	 */
	private final boolean doesDotMatchNewline;// = true;
		
	/**
	 * Value for when {@link #replacement} is the entire match.
	 */
	//public static final String ENTIRE_MATCH = "$0";

	/**
	 * The {@link StringTemplate} that should be substituted evaluated for backreferences,
	 * then returned once for each match, if it is assigned by {@link #setReplacement(StringTemplate)}.
	 */
	private final StringTemplate replacement;// = new StaticStringTemplate(ENTIRE_MATCH);
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * <p>
	 * @see #maxMatch
	 */
	private final int minMatch;// = Pattern.FIRST_MATCH;

	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * <p>
	 * @see #minMatch
	 */
	private final int maxMatch;// = Pattern.LAST_MATCH;
	
	public Find(String serializedString, String uri,
			RegexpCompiler compiler, StringTemplate pattern, StringTemplate replacement,
			int minMatch, int maxMatch,
			boolean isCaseSensitive, boolean isMultiline, boolean doesDotMatchNewline,
			String[] children) {
		super(serializedString, uri);
		this.hasName = false;
		this.name = pattern;
		this.compiler = compiler;
		this.pattern = pattern;
		this.replacement = replacement;
		this.isCaseInsensitive = isCaseSensitive;
		this.isMultiline = isMultiline;
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
		this.doesDotMatchNewline = doesDotMatchNewline;
		this.children = children;
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
	public void execute(String source, Database db, Scope scope)
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
			db.putFailed(scope, source, serialized, uri, "Match " + StringUtils.quote(pattern) +
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
				db.putInstruction(childScope, childSource, children[j], uri);
			}
		}
		
		db.putSuccess(scope, source, this.serialized, this.uri);
	}
}
