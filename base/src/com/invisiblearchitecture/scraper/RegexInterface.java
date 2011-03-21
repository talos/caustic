package com.invisiblearchitecture.scraper;

public interface RegexInterface {
	/**
	 * Equivalent to java.util.regex.Compile.
	 * @param patternString A pattern string to compile.
	 * @return A GeograpePattern.
	 */
	public abstract PatternInterface compile(String patternString);
}
