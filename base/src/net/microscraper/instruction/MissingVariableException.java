package net.microscraper.instruction;

import net.microscraper.client.MicroscraperException;
import net.microscraper.util.Variables;

/**
 * This is thrown when {@link Mustache#compile(String, Variables)} fails because a {@link Variables}
 * instance lacks a tag in the template.
 * @author john
 * @see Variables
 * @see Mustache
 * @see #getName()
 *
 */
public class MissingVariableException extends MicroscraperException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2395999108653919210L;
	private final String name;
	public MissingVariableException(Variables variables, String missingVariableName) {
		this.name  = missingVariableName;
	}
	/**
	 * 
	 * @return The {@link String} name of the key that could not be found.
	 */
	public final String getName() {
		return name;
	}
}
