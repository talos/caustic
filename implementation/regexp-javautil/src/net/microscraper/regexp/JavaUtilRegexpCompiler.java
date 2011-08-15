package net.microscraper.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.MissingGroupException;
import net.microscraper.regexp.NoMatchesException;
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
		
		private String replace(String input, String substitution) throws MissingGroupException {
			Matcher matcher = pattern.matcher(input);
			return matcher.replaceFirst(substitution);
		}
		
		@Override
		public String match(String input, String substitution, int matchNumber) throws NoMatchesException, MissingGroupException {			
			Matcher matcher = pattern.matcher(input);
			List<String> backwardsMemory = new ArrayList<String>();
			int i = 0;
			while(matcher.find()) {
				if(matchNumber >= 0) {
					if(i == matchNumber) {
						return replace(matcher.group(), substitution);
					} else if(i > matchNumber) { break; }
				} else {
					backwardsMemory.add(replace(matcher.group(), substitution));
				}
				i++;
			}
			if(matchNumber < 0 && backwardsMemory.size() + matchNumber >= 0) {
				return backwardsMemory.get(backwardsMemory.size() + matchNumber);
			}
			throw new NoMatchesException(this, i, matchNumber, input);
		}
		
		@Override
		public String[] allMatches(String input, String substitution, int minMatch, int maxMatch)
					throws InvalidRangeException, NoMatchesException, MissingGroupException {
			if((maxMatch >= 0 && minMatch >= 0 && maxMatch < minMatch) ||
					(maxMatch < 0 && minMatch < 0 && maxMatch < minMatch))
				throw new InvalidRangeException(this, minMatch, maxMatch);
			
			Matcher matcher = pattern.matcher(input);
			
			// Find the complete matchesList.
			List<String> matchesList = new ArrayList<String>();
			while(matcher.find()) {
				matchesList.add(replace(matcher.group(), substitution));
			}
			
			// No matches at all.
			if(matchesList.size() == 0)
				throw new NoMatchesException(this, matchesList.size(), minMatch, maxMatch, input);
			
			// Determine the first and last indices relative to our list.
			int firstIndex = minMatch >= 0 ? minMatch : matchesList.size() + minMatch;
			int lastIndex  = maxMatch >= 0 ? maxMatch : matchesList.size() + maxMatch;
			
			// Range excludes 
			if(lastIndex < firstIndex)
				throw new NoMatchesException(this, matchesList.size(), firstIndex, lastIndex, input);
			
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
