package net.microscraper.interfaces.regexp;

/**
 * Parent class for all {@link Exception}s arising from {@link RegexpCompiler}.
 * @author realest
 *
 */
public abstract class RegexpException extends Exception {
	private final PatternInterface pattern;
	public RegexpException(PatternInterface pattern, String message) {
		super(message);
		this.pattern = pattern;
	}
}
