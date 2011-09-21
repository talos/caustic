package net.microscraper.regexp;


import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Encoder;

public class JavaUtilRegexpCompiler implements RegexpCompiler {
	@Override
	public Pattern compile(String patternString, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		int flags = 0;
		flags += isCaseInsensitive ?   java.util.regex.Pattern.CASE_INSENSITIVE : 0;
		flags += isMultiline ?         java.util.regex.Pattern.MULTILINE : 0;
		flags += doesDotMatchNewline ? java.util.regex.Pattern.DOTALL : 0;
		return new JavaUtilPattern(patternString, flags);
	}

	@Override
	public StringTemplate compileTemplate(String templateString,
			String encodedPatternString, String notEncodedPatternString,
			Encoder encoder) {
		return new JavaUtilStringTemplate(templateString, encodedPatternString, 
				notEncodedPatternString, encoder);
	}
}
