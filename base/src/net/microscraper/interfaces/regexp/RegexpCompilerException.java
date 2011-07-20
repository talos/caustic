package net.microscraper.interfaces.regexp;

/**
 * Parent class for all {@link Exception}s arising from {@link RegexpCompiler}.
 * @author realest
 *
 */
public abstract class RegexpCompilerException extends Exception {
	private final PatternInterface pattern;
	public RegexpCompilerException(PatternInterface pattern) {
		this.pattern = pattern;
	}
}
