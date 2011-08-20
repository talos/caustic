package net.microscraper.util;

/**
 * Interface for handling the substitution of {@link Variables} into
 * {@link Object}s.
 * @author talos
 * @see #sub(Variables)
 *
 */
public interface Substitutable {
	
	/**
	 * 
	 * @param variables The {@link Variables} to try in the substitution.
	 * @return A {@link Execution} with either the names of the variables
	 * that were missing, or the substituted version of this {@link Substitutable}.
	 */
	public Execution sub(Variables variables);
}
