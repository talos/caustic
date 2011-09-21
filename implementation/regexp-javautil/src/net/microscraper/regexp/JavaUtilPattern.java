package net.microscraper.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringSubstitution;

final class JavaUtilPattern implements Pattern {
	private final java.util.regex.Pattern pattern;
	
	public JavaUtilPattern(String pString, int flags) {
		pattern = java.util.regex.Pattern.compile(pString, flags);
	}
	
	@Override
	public boolean matches(String input, int matchNumber) {			
		Matcher matcher = pattern.matcher(input);
		int i = 0;
		while(matcher.find()) {
			if(i == matchNumber)
				return true;
			i++;
		}
		boolean match = matcher.find();
		return match;
	}
	
	@Override
	public String[] match(String input, String substitution, int minMatch, int maxMatch) {
		Matcher matcher = pattern.matcher(input);
		
		// Find the complete matchesList.
		List<String> matchesList = new ArrayList<String>();
		while(matcher.find()) {
			matchesList.add(pattern.matcher(matcher.group()).replaceFirst(substitution));
		}
		
		// No matches at all.
		if(matchesList.size() == 0) {
			return new String[] {};
		}
		
		// Determine the first and last indices relative to our list.
		// Adding a negative match to the total size counts backwards.
		int firstIndex = minMatch >= 0 ? minMatch : matchesList.size() + minMatch;
		int lastIndex  = maxMatch >= 0 ? maxMatch : matchesList.size() + maxMatch;

		// Range excludes all matches.
		if(lastIndex < firstIndex)
			return new String[] {};
		
		// First index is after total length
		if(matchesList.size() < firstIndex) {
			return new String[] {};
		}
		
		// Last index must be truncated.
		if(matchesList.size() < lastIndex) {
			lastIndex = matchesList.size() - 1;
		}
		
		return matchesList.subList(firstIndex, lastIndex + 1).toArray(new String[1 + lastIndex - firstIndex]);
	}
	
	@Override
	public StringSubstitution substitute(String input, DatabaseView view) throws DatabaseReadException {
		StringBuffer subbed = new StringBuffer();
		Matcher matcher = pattern.matcher(input);
		List<String> missingTags = new ArrayList<String>();
		
		// build the substituted string
		while(matcher.find()) {
			String tagName = matcher.group();
			String rawTagValue = view.get(tagName);
			
			// only add substituted value if we have it.
			if(rawTagValue == null) {
				missingTags.add(tagName);
			} else {
				matcher.appendReplacement(subbed, rawTagValue.replace("\\", "\\\\")
						.replace("$0", "\\$0")); // these could cause problems in the replacement
			}
		}
		matcher.appendTail(subbed);
		
		if(missingTags.size() > 0) {
			return StringSubstitution.missingTags(missingTags.toArray(new String[missingTags.size()]));
		} else {
			return StringSubstitution.success(subbed.toString());
		}
	}

	@Override
	public String toString() {
		return pattern.toString();
	}
}