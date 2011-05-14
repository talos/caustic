package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;

public abstract class Parser implements Executable {
	protected final Regexp searchRegexp;
	protected final MustacheableString replacement;
	protected Parser(Regexp searchRegexp, MustacheableString replacement) {
		this.searchRegexp = searchRegexp;
		this.replacement = replacement;
	}
}
