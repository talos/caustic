package net.microscraper.regexp;


import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Encoder;

/**
 * An implementation of {@link RegexpCompiler} using
 * {@link java.util.regex.Pattern}.
 * @author talos
 *
 */
public class JavaUtilRegexpCompiler implements RegexpCompiler {
	
	private final Encoder encoder;
	
	/**
	 * Construct a {@link JavaUtilRegexpCompiler}.
	 * @param encoder The {@link Encoder} to use when creating
	 * {@link StringTemplate}s via {@link #newTemplate(String, String, String)}
	 */
	public JavaUtilRegexpCompiler(Encoder encoder) {
		this.encoder = encoder;
	}
	
	@Override
	public Pattern newPattern(String patternString, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		int flags = 0;
		flags += isCaseInsensitive ?   java.util.regex.Pattern.CASE_INSENSITIVE : 0;
		flags += isMultiline ?         java.util.regex.Pattern.MULTILINE : 0;
		flags += doesDotMatchNewline ? java.util.regex.Pattern.DOTALL : 0;
		return new JavaUtilPattern(patternString, flags);
	}

	@Override
	public StringTemplate newTemplate(String templateString,
			String encodedPatternString, String unencodedPatternString) {
		return new JavaUtilStringTemplate(templateString, encodedPatternString, 
				unencodedPatternString, encoder);
	}

	@Override
	public StringTemplate newTemplate(String templateString) {
		return new JavaUtilStringTemplate(templateString, StringTemplate.ENCODED_PATTERN, 
				StringTemplate.UNENCODED_PATTERN, encoder);
	}
}
