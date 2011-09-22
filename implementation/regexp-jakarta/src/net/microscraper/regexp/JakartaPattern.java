package net.microscraper.regexp;

import java.util.Vector;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringSubstitution;

import org.apache.regexp.RE;

final class JakartaPattern implements Pattern {
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
	
	public StringSubstitution substitute(String input, DatabaseView view) throws DatabaseReadException {
		StringBuffer subbed = new StringBuffer();
		Vector missingTags = new Vector();
		
		int pos = 0;
		// loop over all our matches
		while(re.match(input, pos)) {
			// append substring between end of last match and start of current match
			subbed.append(input.substring(pos, re.getParenStart(0)));
			
			String tagName = re.getParen(0);
			String tagValue = view.get(tagName);
			if(tagValue == null) {
				missingTags.add(tagName);
			} else {
				subbed.append(tagValue);
			}
			
			pos = re.getParenEnd(0);
		}
		// append remaining substring
		subbed.append(input.substring(pos));
		
		if(missingTags.size() > 0) {
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return StringSubstitution.missingTags(missingTagsAry);
		} else {
			return StringSubstitution.success(subbed.toString());
		}
	}

	public String toString() {
		return patternString;
	}
}