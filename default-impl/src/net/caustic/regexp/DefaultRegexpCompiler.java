package net.caustic.regexp;

import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.util.DefaultEncoder;

/**
 * A default {@link RegexpCompiler}, using {@link JavaUtilRegexpCompiler}
 * and UTF-8 encoding.
 * @author realest
 *
 */
public class DefaultRegexpCompiler implements RegexpCompiler {

	private final RegexpCompiler c;
	public DefaultRegexpCompiler() {
		c = new JavaUtilRegexpCompiler(new DefaultEncoder());
	}
	
	@Override
	public Pattern newPattern(String patternString, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		return c.newPattern(patternString, isCaseInsensitive, isMultiline, doesDotMatchNewline);
	}

	@Override
	public StringTemplate newTemplate(String templateString,
			String encodedPatternString, String notEncodedPatternString) {
		return c.newTemplate(templateString, encodedPatternString, notEncodedPatternString);
	}

	@Override
	public StringTemplate newTemplate(String templateString) {
		return c.newTemplate(templateString);
	}

}
