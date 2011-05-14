package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;
import net.microscraper.resources.ExecutionContext;

public final class Regexp implements Executable {
	private final MustacheableString pattern;
	private final MustacheableString replacement;
	private final boolean caseInsensitive;
	private final boolean multiline;
	private final boolean dotMatchesNewline;
	public Regexp (MustacheableString pattern, MustacheableString replacement,
					boolean caseInsensitive, boolean multiline,
					boolean dotMatchesNewline) {
		this.pattern = pattern;
		this.replacement = replacement;
		this.caseInsensitive = caseInsensitive;
		this.multiline = multiline;
		this.dotMatchesNewline = dotMatchesNewline;
	}
	public Object execute(ExecutionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}