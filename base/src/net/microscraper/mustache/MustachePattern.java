package net.microscraper.mustache;

import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * A {@link Pattern} that uses a {@link MustacheTemplate} for substitutions.
 * @author john
 *
 */
public class MustachePattern implements Substitutable {
	
	/**
	 * The {@link MustacheTemplate} that will be substituted into a {@link String}
	 * to use as the pattern.
	 */
	private final MustacheTemplate pattern;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#CASE_INSENSITIVE
	 */
	private final boolean isCaseInsensitive;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#MULTILINE
	 */
	private final boolean isMultiline;
	
	/**
	 * Flag equivalent to {@link java.util.regex.Pattern#DOTALL
	 */
	private final boolean doesDotMatchNewline;
	
	/**
	 * The {@link RegexpCompiler} to use when compiling this {@link MustachePattern}.
	 */
	private final RegexpCompiler compiler;
	
	public MustachePattern(RegexpCompiler compiler, MustacheTemplate pattern, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		this.compiler = compiler;
		this.pattern = pattern;
		this.isCaseInsensitive = isCaseInsensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
	
	/**
	 * Compile a {@link MustachePattern} into a {@link Pattern}, which will
	 * be contained in {@link Execution#getExecuted()}.
	 */
	public Execution sub(Variables variables) {
		Execution sub = pattern.sub(variables);
		if(sub.isSuccessful()) {
			String subbedPattern = (String) sub.getExecuted();
			return Execution.success(compiler.compile(
					subbedPattern,
					isCaseInsensitive,
					isMultiline, doesDotMatchNewline));

		} else {
			return sub;
		}
	}
	
	public MustacheTemplate getTemplate() {
		return pattern;
	}
}
