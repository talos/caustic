package net.microscraper.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringSubstitution;
import net.microscraper.util.Encoder;

final class JavaUtilStringTemplate implements StringTemplate {
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
	public StringSubstitution sub(DatabaseView view) throws DatabaseReadException {
		// cannot reuse matchers because this class is accessed concurrently
		Matcher encodedMatcher = encodedPattern.matcher(templateString);
		Matcher notEncodedMatcher = notEncodedPattern.matcher(templateString);
		
		StringBuffer subbed = new StringBuffer();
		List<String> missingTags = new ArrayList<String>();
		
		int pos = 0;
		while(encodedMatcher.find(pos) == true || notEncodedMatcher.find(pos) == true) {
			// if the encoded matcher matches before the not encoded one, the tag is considered
			// encoded TODO handle when they're equal (right now assumes is not encoded)
			boolean isTagEncoded;
			Matcher matcher;
			if(encodedMatcher.regionStart() < notEncodedMatcher.regionEnd()) {
				isTagEncoded = true;
				matcher = encodedMatcher;
			} else {
				isTagEncoded = false;
				matcher = notEncodedMatcher;
			}
			
			// append from end of last match to start of this one
			subbed.append(templateString.substring(pos, matcher.regionStart()));
			String tagName = matcher.group();
			String tagValue = view.get(tagName);
			
			// only add substituted value if we have it.
			if(tagValue == null) {
				missingTags.add(tagName);
			} else {
				if(isTagEncoded) {
					subbed.append(encoder.encode(tagValue));
				} else {
					subbed.append(tagValue);
				}
			}
			
			pos = matcher.end();
		}
		subbed.append(templateString.substring(pos));
		
		if(missingTags.size() > 0) {
			return StringSubstitution.missingTags(missingTags.toArray(new String[missingTags.size()]));
		} else {
			return StringSubstitution.success(subbed.toString());
		}
	}

}
