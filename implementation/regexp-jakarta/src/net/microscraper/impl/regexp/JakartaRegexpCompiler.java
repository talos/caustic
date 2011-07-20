package net.microscraper.impl.regexp;

import java.util.Vector;

import org.apache.regexp.RE;

import net.microscraper.interfaces.regexp.InvalidRangeException;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

public class JakartaRegexpCompiler implements RegexpCompiler {

	public PatternInterface compile(String patternString,
			boolean isCaseInsensitive, boolean isMultiline,
			boolean doesDotMatchNewline) {
		int flags = 0;
		
		flags += isCaseInsensitive   ? RE.MATCH_CASEINDEPENDENT : 0;
		flags += isMultiline         ? RE.MATCH_MULTILINE       : 0;
		flags += doesDotMatchNewline ? RE.MATCH_SINGLELINE      : 0;
		
		return new JakartaPattern(patternString, flags);
	}

	private final static class JakartaPattern implements PatternInterface {
		private final RE re;
		private final String patternString;
		
		public JakartaPattern(String patternString, int flags) {
			this.re = new RE(patternString, flags);
			this.patternString = patternString;
		}
		
		public boolean matches(String input) {
			return re.match(input);
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
		
		private String replace(String input, String substitution) throws MissingGroupException {
			return re.subst(input, substitution, RE.REPLACE_BACKREFERENCES);
		}
		
		public String match(String input, String substitution, int matchNumber) throws NoMatchesException, MissingGroupException {			
			int pos = 0;
			int curMatch = 0;
			Vector backwardsMemory = new Vector();
			while(re.match(input, pos)) {
				pos = re.getParenEnd(0);
				if(matchNumber >= 0) {
					if(curMatch == matchNumber) {
						return replace(re.getParen(0), substitution);
					}
				} else {
					backwardsMemory.addElement(replace(re.getParen(0), substitution));
				}
				curMatch++;
			}
			if(matchNumber < 0 && backwardsMemory.size() + matchNumber >= 0) {
				return (String) backwardsMemory.elementAt(backwardsMemory.size() + matchNumber);
			}
			throw new NoMatchesException(this, curMatch, matchNumber, input);
		}
		
		public String[] allMatches(String input, String substitution, int minMatch, int maxMatch)
					throws InvalidRangeException, NoMatchesException, MissingGroupException {
			if((maxMatch >= 0 && minMatch >= 0 && maxMatch < minMatch) ||
					(maxMatch < 0 && minMatch < 0 && maxMatch < minMatch))
				throw new InvalidRangeException(this, minMatch, maxMatch);
			
			//Matcher matcher = pattern.matcher(input);
			
			// Find the complete matchesList.
			Vector matchesList = new Vector();
			int pos = 0;
			while(re.match(input, pos)) {
				pos = re.getParenEnd(0);
				matchesList.addElement(replace(re.getParen(0), substitution));
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
				matches[i] = (String) matchesList.get(i + firstIndex);
			}
			
			return matches;
		}

		public String toString() {
			return patternString;
		}
	}
}
