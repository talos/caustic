package com.invisiblearchitecture.scraper.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.invisiblearchitecture.scraper.PatternInterface;
import com.invisiblearchitecture.scraper.RegexInterface;

public class JavaUtilRegexInterface implements RegexInterface {
	//private static final List<PatternInterface> patterns = new ArrayList<PatternInterface>();
	@Override
	public PatternInterface compile(String patternString) {
		return new JavaUtilPattern(patternString);
	}
	
	public final class JavaUtilPattern implements PatternInterface {
	/*	private final Map<Integer, Boolean> savedMatches = new HashMap<Integer, Boolean>();
		private final Map<Integer, String[]> savedAllMatches = new HashMap<Integer, String[]>();
		private final Map<Integer, String> savedFirstMatches = new HashMap<Integer, String>();*/
		private Pattern pattern;
		private final int groupCount;
		
		public JavaUtilPattern(String pString) {
			pattern =Pattern.compile(pString);
			groupCount = pattern.matcher("").groupCount();
		}
		
		@Override
		public boolean matches(String input) {
	/*		int inputHashCode = cheapHashCode(input);
			if(savedMatches.containsKey(inputHashCode)) {
				//System.out.println("skipping match, cache size = " + savedMatches.size());
				return savedMatches.get(inputHashCode);
			}*/
			
			Matcher matcher = pattern.matcher(input);
			boolean match = matcher.find();
	//		savedMatches.put(inputHashCode, match);
			return match;
		}
		
		@Override
		public String match(String input, int matchNumber) {
	/*		int inputHashCode = cheapHashCode(input);
			if(savedFirstMatches.containsKey(inputHashCode)) {
				//System.out.println("skipping firstMatch, cache size = " + savedFirstMatches.size());
				return savedFirstMatches.get(inputHashCode);
			}*/
			
			Matcher matcher = pattern.matcher(input);
			int i = 0;
			while(matcher.find()) {
				if(i == matchNumber) {
					if(groupCount > 0) {
						return matcher.group(1);
					} else {
						return matcher.group();
					}
				} else if(i > matchNumber) { break; }
				i++;
			}
			return null;
			//savedFirstMatches.put(inputHashCode, match);
		}
		
		@Override
		public String[] allMatches(String input) {
			if(groupCount == 0) {
				String[] matches = split(input);
				if(matches.length == 0)
					return null;
				return matches;
			}
			Matcher matcher = pattern.matcher(input);
			
			List<String> matchesList = new ArrayList<String>();
			while(matcher.find()) {
				matchesList.add(matcher.group(1));
			}
			if(matchesList.size() == 0)
				return null;
			
			String[] matches = new String[matchesList.size()];
			
			matches = matchesList.toArray(new String[0]);
			
			return matches;
		}

		private String[] split(String input) {
			return pattern.split(input);
		}
		
		@Override
		public String toString() {
			return pattern.toString();
		}

		/*@Override
		public void clearCache() {
			savedMatches.clear();
			savedAllMatches.clear();
			savedFirstMatches.clear();
		}*/
	}
/*
	@Override
	public void clearAllCaches() {
		for(int i = 0; i < patterns.size(); i++) {
			patterns.get(i).clearCache();
		}
	}
	
	private final int numCheapChars = 256;
	private int cheapHashCode(String input) {
		int length = input.length();
		int hashCode;
		if(length > numCheapChars) {
			hashCode = input.substring(0, numCheapChars).hashCode();
		} else {
			hashCode = input.hashCode();
		}
		return hashCode + length;
	}*/
}
