package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseView;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.DependsOnTemplate;
import net.microscraper.template.MissingTags;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.StringUtils;

public class Find implements Instruction {
	
	/**
	 * The {@link StringTemplate} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final StringTemplate pattern;
	
	private boolean hasName = false;
	
	private StringTemplate name;
	
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
	 * Value for when {@link #replacement} is the entire match.
	 */
	public static final String ENTIRE_MATCH = "$0";

	/**
	 * The {@link StringTemplate} that should be substituted evaluated for backreferences,
	 * then returned once for each match, if it is assigned by {@link #setReplacement(StringTemplate)}.
	 */
	private StringTemplate replacement = StringTemplate.staticTemplate(ENTIRE_MATCH);
	
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

	private Instruction[] children = new Instruction[] { };
	
	public Find(RegexpCompiler compiler, StringTemplate pattern) {
		this.compiler = compiler;
		this.pattern = pattern;
	}
	
	public void setChildren(Instruction[] children) {
		this.children = children;
	}
	
	public void setName(StringTemplate name) {
		this.hasName = true;
		this.name = name;
	}
	
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
	 * Use {@link #pattern}, substituted with {@link Variables}, to match against <code>source</code>.
	 */
	public ScraperResult execute(String source, DatabaseView input) throws IOException {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final ScraperResult result;
		final String nameStr;
		final StringSubstitution subPattern = pattern.sub(input);
		final StringSubstitution subReplacement = replacement.sub(input);
		
		if(hasName) {
			StringSubstitution nameSub = name.sub(input);
			if(nameSub.isMissingTags()) {
				return ScraperResult.missingTags(nameSub.getMissingTags()); // break out early
			} else {
				nameStr = nameSub.getSubstituted();
			}
		} else {
			nameStr = null;
		}
				
		if(subPattern.isMissingTags() || subReplacement.isMissingTags()) { // One of the substitutions was not OK.
			result = ScraperResult.missingTags(
					MissingTags.combine( new DependsOnTemplate[] { subPattern, subReplacement } ));
			
		} else { // All the substitutions were OK.
			String patternString = (String) subPattern.getSubstituted();
			Pattern pattern = compiler.compile(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
			
			String replacement = (String) subReplacement.getSubstituted();
			String[] matches = pattern.match(source, replacement, minMatch, maxMatch);
			
			if(matches.length == 0) { // No matches, fail out.
				result = ScraperResult.failure("Match " + StringUtils.quote(pattern) +
					" did not have a match between " + 
					StringUtils.quote(minMatch) + " and " + 
					StringUtils.quote(maxMatch) + " against " +
					StringUtils.quoteAndTruncate(StringUtils.quote(source), 100));
			// We got at least 1 match.
			} else {
				Scraper[] scraperChildren = new Scraper[children.length * matches.length];
				for(int i = 0 ; i < children.length ; i ++) {
					Instruction childInstruction = children[i];
					for(int j = 0 ; j < matches.length ; j ++) {
						DatabaseView childInput;
						String childSource = matches[j];
						if(matches.length == 1) {
							childInput = input;
						} else {
							if(nameStr == null) { // default to using the pattern as a name for the spawned child
								childInput = input.spawnChild(pattern.toString());
							} else {
								childInput = input.spawnChild(nameStr, childSource);
							}
						}
						scraperChildren[i * matches.length + j] =
								new Scraper(childInstruction, childInput, childSource);
					}
				}
				
				// TODO move this to above, invert children/matches
				/*if(children.length == 0) {
					for(int i = 0 ; i < matches.length ; i ++) {
						if(nameStr == null) {
							input.spawnChild(pattern.toString());
						} else{ 
							input.spawnChild(nameStr, matches[i]);
						}
					}
				}*/
				
				if(nameStr == null) {
					result = ScraperResult.success(pattern.toString(), matches, scraperChildren);
				} else {
					result = ScraperResult.success(nameStr, matches, scraperChildren);
				}
			}
		}
		return result;
	}
	
	/**
	 * @return The raw pattern template.
	 */
	public String toString() {
		return pattern.toString();
	}
}
