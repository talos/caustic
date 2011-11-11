package net.caustic.regexp;


import org.apache.regexp.RE;

import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.util.Encoder;

/**
 * An implementation of {@link RegexpCompiler} using {@link org.apache.regexp.RE}.
 * @author talos
 *
 */
public class JakartaRegexpCompiler implements RegexpCompiler {

	private final Encoder encoder;
	public JakartaRegexpCompiler(Encoder encoder) {
		this.encoder = encoder;
	}
	
	public Pattern newPattern(String patternString,
			boolean isCaseInsensitive, boolean isMultiline,
			boolean doesDotMatchNewline) {
		int flags = 0;
		
		flags += isCaseInsensitive   ? RE.MATCH_CASEINDEPENDENT : 0;
		flags += isMultiline         ? RE.MATCH_MULTILINE       : 0;
		flags += doesDotMatchNewline ? RE.MATCH_SINGLELINE      : 0;
		
		return new JakartaPattern(patternString, flags);
	}

	public StringTemplate newTemplate(String templateString,
			String encodedPatternString, String unencodedPatternString) {
		return new JakartaStringTemplate(templateString, encodedPatternString,
				unencodedPatternString, encoder);
	}

	public StringTemplate newTemplate(String templateString) {
		return new JakartaStringTemplate(templateString, StringTemplate.ENCODED_PATTERN,
				StringTemplate.UNENCODED_PATTERN, encoder);	}
}
