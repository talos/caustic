package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonObject;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.MissingGroupException;
import net.microscraper.regexp.NoMatchesException;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;

/**
 * {@link Find} provides a pattern and a replacement value for matches.
 * @author john
 *
 */
public class Find {

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
	
	public Execution matchAgainst(String source, Variables variables)
					throws NoMatchesException, MissingGroupException,
					InvalidRangeException {
		return pattern.compile(compiler, variables).match(
				source,
				replacement.sub(variables),
				minMatch, maxMatch);
	}
	
}
