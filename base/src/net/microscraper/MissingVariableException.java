package net.microscraper;

/**
 * This is thrown when {@link Mustache#compile(String, Variables)} fails because a {@link Variables}
 * instance lacks a tag in the template.
 * @author john
 * @see Variables
 * @see Mustache
 *
 */
public class MissingVariableException extends ClientException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2395999108653919210L;
	public final String name;
	public MissingVariableException(Variables variables, String missingVariableName) {
		this.name  = missingVariableName;
	}
}
