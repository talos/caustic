package net.microscraper.instruction;


import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabasePersistException;
import net.microscraper.database.DatabaseReadException;
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
	public ScraperResult execute(String source, DatabaseView inputView) throws DatabasePersistException, DatabaseReadException {
		if(source == null) {
			throw new IllegalArgumentException("Cannot execute Find without a source.");
		}
		
		final ScraperResult result;
		final StringSubstitution subName;
		final StringSubstitution subPattern = pattern.sub(inputView);
		final StringSubstitution subReplacement = replacement.sub(inputView);
		
		if(hasName) {
			subName = name.sub(inputView);
		} else {
			subName = subPattern; // if no name defined, default to the pattern.
		}
				
		if(subName.isMissingTags() ||
				subPattern.isMissingTags() ||
				subReplacement.isMissingTags()) { // One of the substitutions was not OK.
			result = ScraperResult.missingTags(MissingTags.combine(
					new DependsOnTemplate[] { subName, subPattern, subReplacement } ));
			
		} else { // All the substitutions were OK.
			String resultName = subName.getSubstituted();
			
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
				DatabaseView[] resultViews = new DatabaseView[matches.length];
				Scraper[] scraperChildren = new Scraper[children.length * matches.length];
				for(int i = 0 ; i < matches.length ; i ++) {
					
					// generate result views.
					String childSource = matches[i];
					if(matches.length == 1) { // don't spawn a new result for single match
						resultViews[i] = inputView;
						if(hasName) {
							inputView.put(resultName, childSource);
						}
					} else {
						if(hasName) {
							resultViews[i] = inputView.spawnChild(resultName, childSource);
						} else {
							resultViews[i] = inputView.spawnChild(resultName);							
						}
					}
					
					// generate children from result views
					DatabaseView childView = resultViews[i];
					for(int j = 0 ; j < children.length ; j ++) {
						Instruction childInstruction = children[j];
						
						scraperChildren[i * children.length + j] =
								new Scraper(childInstruction, childView, childSource);
					}
				}
				
				result = ScraperResult.success(resultName, resultViews, scraperChildren);
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
