package net.microscraper.util;

import java.util.Vector;

/**
 * {@link Substitution} says whether an attempt to use a set of {@link Variables}
 * to modify an {@link Object} was successful.  If it was, it returns the modified
 * {@link Object}; if it wasn't, it returns an array of the variable names that
 * were missing.
 * @author talos
 *
 */
public class Substitution {
	
	private final Object substituted;
	private final String[] missingVariables;
	private Substitution(Object substituted, String[] missingVariables) {
		this.substituted = substituted;
		this.missingVariables = missingVariables;
	}
	
	/**
	 * @param substituted The successfully substituted {@link Object}.
	 * @return A successful {@link Substitution}.
	 */
	public static Substitution success(Object substituted) {
		return new Substitution(substituted, null);
	}

	/**
	 * @param missingVariables the {@link String} variable names that were
	 * missing.  Must be of non-zero length.
     * @return A failed {@link Substitution}.
	 */
	public static Substitution fail(String[] missingVariables) {
		if(missingVariables.length == 0)
			throw new IllegalArgumentException();
		return new Substitution(null, missingVariables);
	}
	
	/**
	 * 
	 * @return Whether this {@link Substitution} was successful.
	 * @see #getSubstituted()
	 * @see #getMissingVariables()
	 */
	public boolean isSuccessful() {
		return missingVariables == null ? true : false;
	}

	/**
	 * 
	 * @return The successfully substituted object of a {@link Substitution}.
	 * Should only be called if {@link #isSuccessful()} is <code>true</code>.
	 * @see #isSuccessful()
	 */
	public Object getSubstituted() {
		if(isSuccessful() == false)
			throw new IllegalStateException();
		return substituted;
	}
	/**
	 * 
	 * @return The tag that caused an unsuccessful {@link Substitution}.
	 * Should only be called if {@link #isSuccessful()} is <code>false</code>.
	 * @see #isSuccessful()
	 */
	public String[] getMissingVariables() {
		if(isSuccessful() == true)
			throw new IllegalStateException();
		return missingVariables;
	}
	
	/**
	 * Combine an array of {@link Substition}s into a single {@link Substitution},
	 * with an unsuccessful status if any member of <code>substitutions</code> was
	 * unsuccessful.  All unique missing variables are combined.<p>
	 * If all <code>substitutions</code> were successful, their substituted value
	 * is an array of the successful objects.
	 * @param substitutions An array of {@link Substitution}s.
	 * @return A single {@link Substitution}.
	 */
	public static Substitution combine(Substitution[] substitutions) {
		Object[] combinedObject = new Object[substitutions.length];
		Vector combinedMissingVariables = new Vector();
		for(int i = 0 ; i < substitutions.length ; i ++) {
			if(substitutions[i].isSuccessful()) {
				combinedObject[i] = substitutions[i].getSubstituted();
			} else {
				String[] missingVariables = substitutions[i].getMissingVariables();
				for(int j = 0 ; j < missingVariables.length ; j ++) {
					//combinedMissingVariables.put(missingVariables[i], new Object());
					if(!combinedMissingVariables.contains(missingVariables[j])) {
						combinedMissingVariables.add(missingVariables[j]);
					}
				}
			}
		}
		if(combinedMissingVariables.size() > 0) {
			String[] combinedMissingVariablesAry = new String[combinedMissingVariables.size()];
			combinedMissingVariables.copyInto(combinedMissingVariablesAry);
			return Substitution.fail(combinedMissingVariablesAry);
		} else {
			return Substitution.success(combinedObject);
		}
	}
	
	/**
	 * Convenience method to generate a single {@link Substitution} from a whole
	 * array of {@link Substitutables}. The {@link #getSubstituted()} of the
	 * returned {@link Substitution} will be an array of the substituted <code>
	 * substitutables</code>, if it is successful.
	 * @param substitutables The array of {@link Substitutable}s to {@link 
	 * Substitutable#sub(Variables)} en masse.
	 * @param variables THe {@link Variables} to use.
	 * @return A single {@link Substitution}, with either all the <code>substitutables</code>
	 * substituted or a combined array of the missing variables.
	 */
	public static Substitution arraySub(Substitutable[] substitutables, Variables variables) {
		Substitution[] substitutions = new Substitution[substitutables.length];
		for(int i = 0 ; i < substitutables.length ; i ++) {
			substitutions[i] = substitutables[i].sub(variables);
		}
		return combine(substitutions);
	}
}
