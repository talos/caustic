package net.microscraper.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Vector;

import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.regexp.Pattern;

/**
 * {@link Execution} says whether an attempt to use a set of {@link Variables}
 * to modify an {@link Object} was successful.  If it was, it contains the modified
 * {@link Object}; if it wasn't because there was a {@Link Variable} missing, it contains
 * an array of the missing {@link Variable} names; if it wasn't because there was
 * some other problem, it contains a description of the problem.
 * @author talos
 *
 */
public class Execution {
	
	private final Object executed;
	private final String[] missingVariables;
	private final String[] failedBecause;
	
	/**
	 * Private constructor.  Only one of these parameters should be non-<code>null</code>
	 * @param executed The {@link Object} returned by {@link #getExecuted()}.
	 * @param missingVariables The {@link String} array returned by {@link #getMissingVariables()},
	 * 
	 * @param failedBecause
	 */
	private Execution(Object executed, String[] missingVariables, String[] failedBecause) {
		this.executed = executed;
		this.missingVariables = missingVariables;
		this.failedBecause = failedBecause;
	}
	
	/**
	 * @param executed The successfully executed {@link Object}.
	 * @return A successful {@link Execution}.
	 */
	public static Execution success(Object executed) {
		return new Execution(executed, null, null);
	}

	/**
	 * @param missingVariables the {@link String} variable names that were
	 * missing.  Must be of non-zero length.
     * @return A failed {@link Execution}.
	 */
	public static Execution missingVariables(String[] missingVariables) {
		if(missingVariables.length == 0)
			throw new IllegalArgumentException();
		return new Execution(null, missingVariables, null);
	}

	/**
	 * Create an {@link Execution} describing a failure because a pattern did 
	 * not match against a source.
	 * @param source The {@link String} source.
	 * @param pattern The {@link Pattern} that did not match.
	 * @param minMatch The expected first match.
	 * @param maxMatch The expected last match.
	 * @return The failed {@link Execution}.
	 */
	public static Execution noMatches(String source, Pattern pattern, int minMatch, int maxMatch) {
		return new Execution(null, null,
				new String[] { "Match " + StringUtils.quote(pattern) +
						" did not have a match between " + 
						StringUtils.quote(minMatch) + " and " + 
						StringUtils.quote(maxMatch) + " against " +
						StringUtils.quote(source)
		});
	}
	
	/**
	 * Create an {@link Execution} describing a failure because of an {@link IOException}.
	 * @param e The {@link IOException} that caused the {@link Execution} to fail.
	 * @return The failed {@link Execution}.
	 */
	public static Execution ioException(IOException e) {
		return new Execution(null, null, new String[] { e.getMessage() });
	}
	
	/**
	 * Create an {@link Execution} describing a failure because a test patterns
	 * failed.
	 * @param tested The {@link String} being tested.
	 * @param tests A {@link Pattern} doing the testing.
	 * @return The failed {@link Execution}.
	 */
	public static Execution failedTests(String tested, Pattern test) {
		return new Execution(null, null,
				new String[] { "Test " + StringUtils.quote(test) +
				" did not match " +
				StringUtils.quote(tested)
		});
	}
	/**
	 * 
	 * @return Whether this {@link Execution} was successful.
	 * @see #getExecuted()
	 * @see #getMissingVariables()
	 */
	public boolean isSuccessful() {
		return executed == null ? false : true;
	}

	/**
	 * 
	 * @return The successfully executed object of a {@link Execution}.
	 * Should only be called if {@link #isSuccessful()} is <code>true</code>.
	 * @see #isSuccessful()
	 */
	public Object getExecuted() {
		if(isSuccessful() == false)
			throw new IllegalStateException();
		return executed;
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} is missing variables, <code>false</code> otherwise.
	 * @see #getMissingVariables()
	 */
	public boolean isMissingVariables() {
		return missingVariables == null ? false : true;
	}
	
	/**
	 * 
	 * @return The tag that caused an unsuccessful {@link Execution}.
	 * Should only be called if {@link #isSuccessful()} is <code>false</code>.
	 * @see #isSuccessful()
	 */
	public String[] getMissingVariables() {
		if(!isMissingVariables()) 
			throw new IllegalStateException();
		return missingVariables;
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has failed, <code>false</code> otherwise.
	 * @see #failedBecause()
	 */
	public boolean hasFailed() {
		return failedBecause == null ? false : true;
	}
	
	/**
	 * 
	 * @return What caused the {@link Execution} to fail.
	 * @see #hasFailed()
	 */
	public String[] failedBecause() {
		if(!hasFailed())
			throw new IllegalStateException();
		return failedBecause;
	}
	
	/**
	 * Combine an array of {@link Execution}s into a single {@link Execution},
	 * with a successful status if all the components are successful, a missing variable
	 * status if at least one is missing variables but non have failed, and a failed
	 * status if at least one has failed.<p>
	 * All unique missing variable names are combined.<p>
	 * If all <code>executions</code> were successful, the result {@link Execution}'s
	 * executed value is an {@link Object} array of the executed objects.
	 * Each must be cast individually.
	 * @param executions An array of {@link Execution}s.
	 * @return A single {@link Execution}.
	 */
	public static Execution combine(Execution[] executions) {
		Object[] combinedObject = new Object[executions.length];
		Vector combinedMissingVariables = new Vector();
		Vector combinedFailedBecause = new Vector();
		for(int i = 0 ; i < executions.length ; i ++) {
			if(executions[i].isSuccessful()) {
				combinedObject[i] = executions[i].getExecuted();
			} else if(executions[i].isMissingVariables()) {
				String[] missingVariables = executions[i].getMissingVariables();
				for(int j = 0 ; j < missingVariables.length ; j ++) {
					//combinedMissingVariables.put(missingVariables[i], new Object());
					if(!combinedMissingVariables.contains(missingVariables[j])) {
						combinedMissingVariables.add(missingVariables[j]);
					}
				}
			} else if(executions[i].hasFailed()) {
				String[] failedBecause = executions[i].failedBecause();
				for(int j = 0 ; j < failedBecause.length ; j ++) {
					combinedFailedBecause.add(failedBecause[j]);
				}
			}
		}
		
		// Failures take precedence.
		if(combinedFailedBecause.size() > 0) {
			String[] combinedFailedBecauseAry = new String[combinedFailedBecause.size()];
			combinedFailedBecause.copyInto(combinedFailedBecauseAry);
			return new Execution(null, null, combinedFailedBecauseAry);
			
		// Then missing variables
		} else if(combinedMissingVariables.size() > 0) {
			String[] combinedMissingVariablesAry = new String[combinedMissingVariables.size()];
			combinedMissingVariables.copyInto(combinedMissingVariablesAry);
			return Execution.missingVariables(combinedMissingVariablesAry);
		
		// Lastly success.
		} else {
			return Execution.success(combinedObject);
		}
	}
	
	/**
	 * Convenience method to generate a single {@link Execution} from a whole
	 * array of {@link Substitutables}. The {@link #getExecuted()} of the
	 * returned {@link Execution} will be an {@link Object} array of the substituted <code>
	 * substitutables</code>, if it is successful.  Each element must
	 * be cast separately.
	 * @param substitutables The array of {@link Substitutable}s to {@link 
	 * Substitutable#sub(Variables)} en masse.
	 * @param variables The {@link Variables} to use.
	 * @return A single {@link Execution}, with either all the <code>substitutables</code>
	 * substituted or a combined array of the missing variables.
	 */
	public static Execution arraySub(Substitutable[] substitutables, Variables variables) {
		Execution[] substitutions = new Execution[substitutables.length];
		for(int i = 0 ; i < substitutables.length ; i ++) {
			substitutions[i] = substitutables[i].sub(variables);
		}
		return combine(substitutions);
	}
	

	/**
	 * Convenience method to generate a single {@link Execution} from a whole
	 * array of {@link MustachePattern}s. The {@link #getExecuted()} of the
	 * returned {@link Execution} will be an array of the substituted {@link
	 * Pattern}s if it is successful.
	 * @param substitutables The array of {@link MustacheNameValuePair}s to {@link 
	 * MustacheNameValuePair#sub(Variables)} en masse.
	 * @param variables The {@link Variables} to use.
	 * @return A single {@link Execution}, with either all the <code>substitutables</code>
	 * substituted or a combined array of the missing variables.
	 */
	public static Execution arraySubNameValuePair(MustacheNameValuePair[] substitutables, Variables variables) {
		Execution[] substitutions = new Execution[substitutables.length];
		for(int i = 0 ; i < substitutables.length ; i ++) {
			substitutions[i] = substitutables[i].sub(variables);
		}
		//return combine(substitutions);
		Execution result;
		Execution rawCombined = combine(substitutions);
		if(rawCombined.isSuccessful()) {
			Object[] objArray = (Object[]) rawCombined.getExecuted();
			NameValuePair[] nameValuePairArray = new NameValuePair[objArray.length];
			for(int i = 0 ; i < objArray.length ; i ++) {
				nameValuePairArray[i] = (NameValuePair) objArray[i];
			}
			result = Execution.success(nameValuePairArray);
		} else {
			result = rawCombined;
		}
		return result;
	}
	
	/**
	 * Convenience method to generate a single {@link Execution} from a whole
	 * array of {@link MustacheNameValuePair}s. The {@link #getExecuted()} of the
	 * returned {@link Execution} will be an array of the substituted {@link
	 * NameValuePair}s if it is successful.
	 * @param substitutables The array of {@link MustachePattern}s to {@link 
	 * MustachePattern#sub(Variables)} en masse.
	 * @param variables The {@link Variables} to use.
	 * @return A single {@link Execution}, with either all the <code>substitutables</code>
	 * substituted or a combined array of the missing variables.
	 */
	public static Execution arraySubPattern(MustachePattern[] patterns, Variables variables) {
		Execution[] substitutions = new Execution[patterns.length];
		for(int i = 0 ; i < patterns.length ; i ++) {
			substitutions[i] = patterns[i].sub(variables);
		}
		//return combine(substitutions);
		Execution result;
		Execution rawCombined = combine(substitutions);
		if(rawCombined.isSuccessful()) {
			Object[] objArray = (Object[]) rawCombined.getExecuted();
			Pattern[] patternAry = new Pattern[objArray.length];
			for(int i = 0 ; i < objArray.length ; i ++) {
				patternAry[i] = (Pattern) objArray[i];
			}
			result = Execution.success(patternAry);
		} else {
			result = rawCombined;
		}
		return result;
	}
}
