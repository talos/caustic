package net.microscraper.regexp;

/**
 * Parent class for all {@link Exception}s arising from {@link RegexpCompiler}.
 * @author realest
 *
 */
public abstract class RegexpException extends Exception {
	private final Pattern pattern;
	public RegexpException(Pattern pattern, String message) {
		super(message);
		this.pattern = pattern;
	}
}
