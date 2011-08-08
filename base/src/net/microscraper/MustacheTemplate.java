package net.microscraper;

/**
 * A string where Mustache-style demarcation denotes where Variables are taken out.
 * @author john
 *
 */
public final class MustacheTemplate {
	private final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 */
	public MustacheTemplate(String string) {
		this.string = string;
	}
	
	/**
	 * Mustache-compile this {@link MustacheTemplate}.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link URL}'s compiled url as a {@link java.net.url}.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 * @throws MustacheTemplateException If the {@link MustacheTemplate} was invalid.
	 */
	public String compile(Variables variables) throws MissingVariableException, MustacheTemplateException {
		return Mustache.compile(string, variables);
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link MustacheTemplate}.
	 */
	public String toString() {
		return string;
	}
}
