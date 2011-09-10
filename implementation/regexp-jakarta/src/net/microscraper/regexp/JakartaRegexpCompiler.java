package net.microscraper.regexp;

import java.util.Vector;

import org.apache.regexp.RE;

import net.microscraper.regexp.InvalidRangeException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;

public class JakartaRegexpCompiler implements RegexpCompiler {

	public Pattern compile(String patternString,
			boolean isCaseInsensitive, boolean isMultiline,
			boolean doesDotMatchNewline) {
		int flags = 0;
		
		flags += isCaseInsensitive   ? RE.MATCH_CASEINDEPENDENT : 0;
		flags += isMultiline         ? RE.MATCH_MULTILINE       : 0;
		flags += doesDotMatchNewline ? RE.MATCH_SINGLELINE      : 0;
		
		return new JakartaPattern(patternString, flags);
	}

	private final static class JakartaPattern implements Pattern {
		private final RE re;
		private final String patternString;
		
		public JakartaPattern(String patternString, int flags) {
			this.re = new RE(patternString, flags);
			this.patternString = patternString;
		}
		
		public boolean matches(String input, int matchNumber) {
			int pos = 0;
			int curMatch = 0;
			while(re.match(input, pos)) {
				if(curMatch == matchNumber) {
					return true;
				}
				pos = re.getParenEnd(0);
				curMatch++;
			}
			return false;
		}
		
		public String[] match(String input, String substitution, int minMatch, int maxMatch) {
			if((maxMatch >= 0 && minMatch >= 0 && maxMatch < minMatch) ||
					(maxMatch < 0 && minMatch < 0 && maxMatch < minMatch))
				throw new IllegalArgumentException(new InvalidRangeException(this, minMatch, maxMatch));
			
			//Matcher matcher = pattern.matcher(input);
			
			// Find the complete matchesList.
			Vector matchesList = new Vector();
			int pos = 0;
			while(re.match(input, pos)) {
				pos = re.getParenEnd(0);
				matchesList.addElement(re.subst(re.getParen(0), substitution, RE.REPLACE_BACKREFERENCES));
			}
			// No matches at all.
			if(matchesList.size() == 0)
				return new String[] {};
			
			// Determine the first and last indices relative to our list.
			int firstIndex = minMatch >= 0 ? minMatch : matchesList.size() + minMatch;
			int lastIndex  = maxMatch >= 0 ? maxMatch : matchesList.size() + maxMatch;
			
			// Range excludes 
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
			
			String[] matches = new String[1 + lastIndex - firstIndex];
			for(int i = 0 ; i < matches.length ; i ++) {
				matches[i] = (String) matchesList.get(i + firstIndex);
			}
			
			return matches;
		}

		public String toString() {
			return patternString;
		}
	}
}
