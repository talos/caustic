package net.microscraper.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.microscraper.client.Interfaces.Regexp;

public class JavaUtilRegexInterface implements Regexp {
	@Override
	public Pattern compile(String patternString, boolean isCaseInsensitive,
			boolean isMultiline, boolean doesDotMatchNewline) {
		int flags = 0;
		flags += isCaseInsensitive ? java.util.regex.Pattern.CASE_INSENSITIVE : 0;
		flags += isMultiline ? java.util.regex.Pattern.MULTILINE : 0;
		flags += doesDotMatchNewline ? java.util.regex.Pattern.DOTALL : 0;
		return new JavaUtilPattern(patternString, flags);
	}
	
	private final static class JavaUtilPattern implements Pattern {
		private java.util.regex.Pattern pattern;
		
		public JavaUtilPattern(String pString, int flags) {
			pattern = java.util.regex.Pattern.compile(pString, flags);
		}
		
		@Override
		public boolean matches(String input) {			
			Matcher matcher = pattern.matcher(input);
			boolean match = matcher.find();
			return match;
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
		
		private String replace(String input, String substitution) throws MissingGroup {
			Matcher matcher = pattern.matcher(input);
			return matcher.replaceFirst(substitution);
		}
		
		@Override
		public String match(String input, String substitution, int matchNumber) throws NoMatches, MissingGroup {			
			Matcher matcher = pattern.matcher(input);
			int i = 0;
			while(matcher.find()) {
				if(i == matchNumber) {
					return replace(matcher.group(), substitution);
				} else if(i > matchNumber) { break; }
				i++;
			}
			throw new NoMatches(this, input);
		}
		
		@Override
		public String[] allMatches(String input, String substitution) throws NoMatches, MissingGroup {
			Matcher matcher = pattern.matcher(input);
			
			List<String> matchesList = new ArrayList<String>();
			while(matcher.find()) {
				matchesList.add(replace(matcher.group(), substitution));
			}
			if(matchesList.size() == 0)
				throw new NoMatches(this, input);
			
			String[] matches = matchesList.toArray(new String[0]);
			return matches;
		}

		@Override
		public String toString() {
			return pattern.toString();
		}
	}
}
