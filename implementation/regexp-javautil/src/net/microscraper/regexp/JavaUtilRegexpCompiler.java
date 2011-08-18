package net.microscraper.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;

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
	
	private final static class JavaUtilPattern implements Pattern {
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
			if(!RegexpUtils.isValidRange(minMatch, maxMatch)) {
				throw new IllegalArgumentException(new InvalidRangeException(this, minMatch, maxMatch));
			}
			
			Matcher matcher = pattern.matcher(input);
			
			// Find the complete matchesList.
			List<String> matchesList = new ArrayList<String>();
			while(matcher.find()) {
				matchesList.add(pattern.matcher(matcher.group()).replaceFirst(substitution));
			}
			
			// No matches at all.
			if(matchesList.size() == 0)
				return new String[] {};
			
			// Determine the first and last indices relative to our list.
			int firstIndex = minMatch >= 0 ? minMatch : matchesList.size() + minMatch;
			int lastIndex  = maxMatch >= 0 ? maxMatch : matchesList.size() + maxMatch;
			
			// Range excludes all matches.
			if(lastIndex < firstIndex)
				return new String[] {};
			
			String[] matches = new String[1 + lastIndex - firstIndex];
			for(int i = 0 ; i < matches.length ; i ++) {
				matches[i] = matchesList.get(i + firstIndex);
			}
			
			return matches;
		}

		@Override
		public String toString() {
			return pattern.toString();
		}
	}
}
