package net.caustic.regexp;

import java.util.Vector;

import org.apache.regexp.RE;

import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.Encoder;

public class JakartaStringTemplate implements StringTemplate {
	private static final int NOT_MATCHED = -1;
	
	private final RE encodedPattern;
	private final RE notEncodedPattern;
	private final String templateString;
	private final Encoder encoder;
	
	public JakartaStringTemplate(String templateString,
			String encodedPatternString, String notEncodedPatternString,
			Encoder encoder) {
		this.encodedPattern = new RE(encodedPatternString);
		this.notEncodedPattern = new RE(notEncodedPatternString);
		this.templateString = templateString;
		this.encoder = encoder;
	}
	
	public StringSubstitution sub(DatabaseView view)
			throws DatabaseException {
		StringBuffer buf = new StringBuffer();
		Vector missingTags = new Vector();
		int pos = 0;
		do {
			// gather match data for both encoded and not-encoded patterns.
			int encBegin =    NOT_MATCHED;
			int notEncBegin = NOT_MATCHED;
			String encMatch = null;
			String notEncMatch = null;
			int encEnd = NOT_MATCHED;
			int notEncEnd = NOT_MATCHED;
			if(encodedPattern.match(templateString, pos)) {
				encBegin = encodedPattern.getParenStart(0);
				encMatch = encodedPattern.getParen(1);
				encEnd = encodedPattern.getParenEnd(0);
			}
			if(notEncodedPattern.match(templateString, pos)) {
				notEncBegin = notEncodedPattern.getParenStart(0);
				notEncMatch = notEncodedPattern.getParen(1);
				notEncEnd = notEncodedPattern.getParenEnd(0);
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
			String value = view.get(tagName);
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
		
		// append any trailing characters
		buf.append(templateString.substring(pos));
		
		if(missingTags.size() > 0) { // missing tags
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return StringSubstitution.missingTags(missingTagsAry);
		} else { // success!
			return StringSubstitution.success(buf.toString());
		}
	}
	
	/**
	 * String representation is the raw template string.
	 */
	public String toString() {
		return templateString;
	}

}
