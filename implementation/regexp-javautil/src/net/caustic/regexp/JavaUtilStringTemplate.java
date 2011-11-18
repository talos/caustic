package net.caustic.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.template.StringSubstitution;
import net.caustic.util.Encoder;

final class JavaUtilStringTemplate extends StringTemplate {
	private static final int NOT_MATCHED = -1;

	private final java.util.regex.Pattern encodedPattern;
	private final java.util.regex.Pattern notEncodedPattern;
	private final String templateString;
	private final Encoder encoder;
	
	public JavaUtilStringTemplate(String templateString,
			String encodedPatternString, String notEncodedPatternString,
			Encoder encoder) {
		this.encodedPattern = java.util.regex.Pattern.compile(encodedPatternString);
		this.notEncodedPattern = java.util.regex.Pattern.compile(notEncodedPatternString);
		this.templateString = templateString;
		this.encoder = encoder;
	}

	@Override
	public StringSubstitution sub(Database db, Scope scope) throws DatabaseException {
		// cannot reuse matchers because this class is accessed concurrently
		Matcher encodedMatcher = encodedPattern.matcher(templateString);
		Matcher notEncodedMatcher = notEncodedPattern.matcher(templateString);
		
		StringBuffer buf = new StringBuffer();
		List<String> missingTags = new ArrayList<String>();
		
		int pos = 0;		
		do {			// gather match data for both encoded and not-encoded patterns.
			int encBegin =    NOT_MATCHED;
			int notEncBegin = NOT_MATCHED;
			String encMatch = null;
			String notEncMatch = null;
			int encEnd = NOT_MATCHED;
			int notEncEnd = NOT_MATCHED;
			if(encodedMatcher.find(pos)) {
				encBegin = encodedMatcher.start();
				encMatch = encodedMatcher.group(1);
				encEnd = encodedMatcher.end();
			}
			if(notEncodedMatcher.find(pos)) {
				notEncBegin = notEncodedMatcher.start();
				notEncMatch = notEncodedMatcher.group(1);
				notEncEnd = notEncodedMatcher.end();
			}

			// determine whether an encoded or not encoded pattern
			// matches first.
			int begin, end;
			String tagName;
			boolean encoded;
			if(notEncBegin != NOT_MATCHED && (encBegin == NOT_MATCHED || notEncBegin < encBegin)) {
				begin = notEncBegin;
				end = notEncEnd;
				tagName = notEncMatch;
				encoded = false;
			} else if(encBegin != NOT_MATCHED && (notEncBegin == NOT_MATCHED || encBegin <= notEncBegin)) {
				begin = encBegin;
				end = encEnd;
				tagName = encMatch;
				encoded = true;
			} else { // no matches, break out.
				break;
			}

			// add unmatched previous string
			buf.append(templateString.substring(pos, begin));
			
			// retrieve & append value
			String value = db.get(scope, tagName);
			if(value != null) {
				if(encoded) {
					buf.append(encoder.encode(value));
				} else {
					buf.append(value);
				}
			} else {
				missingTags.add(tagName);
			}
			
			// advance next match to the end of this one
			pos = end;
		} while(pos < templateString.length());
		
		buf.append(templateString.substring(pos));
		
		if(missingTags.size() > 0) {
			return StringSubstitution.missingTags(missingTags.toArray(new String[missingTags.size()]));
		} else {
			return StringSubstitution.success(buf.toString());
		}
	}

	@Override
	protected String asString() {
		return templateString;
	}

}
