package net.microscraper.execution;

import java.util.Hashtable;

import net.microscraper.client.MissingVariableException;

/**
 * An {@link Execution} that has {@link VariablesExecution}s.  It also
 * functions as a {@link Hashtable} for these Execution's values.
 * @author realest
 * @see Hashtable
 *
 */
public interface HasVariableExecutions extends Execution {
	/**
	 * 
	 * @param key A String, corresponds to {@link VariableExecution#getName}.
	 * @return The {@link VariableExecution}'s result.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this Execution contains no VariableExecution
	 * with a {@link VariableExecution#getName} corresponding to <code>key</code>. 
	 * @see Hashtable#get
	 */
	public String get(String key) throws MissingVariableException;
	
	/**
	 * Tests if the specified object is a key in this {@link HasVariableExecutions}. 
	 * @param key possible key 
	 * @return <code>true</code> if and only if the specified String is a key
	 * in this {@link HasVariableExecutions}.
	 * @throws NullPointerException if the key is <code>null</code>
	 * @see Hashtable#containsKey
	 */
	public boolean containsKey(String key);
	
	
	public VariableExecution[] getVariableExecutions();

}
