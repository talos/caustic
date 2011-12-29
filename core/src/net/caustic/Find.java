package net.caustic;

import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.DependsOnTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.StaticStringTemplate;
import net.caustic.util.StringMap;
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
	
	//private final boolean hasName;
	
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
	
	public Find(String description, String uri,
			RegexpCompiler compiler, StringTemplate name,
			StringTemplate pattern, StringTemplate replacement,
			int minMatch, int maxMatch,
			boolean isCaseSensitive, boolean isMultiline, boolean doesDotMatchNewline,
			String[] children) {
		super(description, uri);
		//this.hasName = hasName;
		this.name = name;
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
	public Response execute(String id, String input, StringMap tags) {
		if(input == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		final Response result;
		
		final StringSubstitution subName = name.sub(tags);
		final StringSubstitution subPattern = pattern.sub(tags);
		final StringSubstitution subReplacement = replacement.sub(tags);
				
		if(subName.isMissingTags() ||
				subPattern.isMissingTags() ||
				subReplacement.isMissingTags()) { // One of the substitutions was not OK.
			final String[] missingTags = StringSubstitution.combine(
					new DependsOnTemplate[] { subName, subPattern, subReplacement });
			result = new Response.MissingTags(id, uri, description, missingTags);
		} else {
			
			// All the substitutions were OK.
			String resultName = subName.getSubstituted();
			
			String patternString = (String) subPattern.getSubstituted();
			Pattern pattern = compiler.newPattern(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
			
			String replacement = (String) subReplacement.getSubstituted();
			String[] matches = pattern.match(input, replacement, minMatch, maxMatch);
			
			if(matches.length == 0) { // No matches, fail out.
				result = new Response.Failed(id, uri, description, "Match " + StringUtils.quote(pattern) +
						" did not have a match between " + 
						StringUtils.quote(minMatch) + " and " + 
						StringUtils.quote(maxMatch) + " against " + StringUtils.quote(input));
			} else {
				result = new Response.DoneFind(id, uri, description, children, resultName, matches);
			}
		}
		return result;
	}
}
