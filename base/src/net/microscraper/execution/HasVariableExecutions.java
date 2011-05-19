package net.microscraper.execution;

import java.util.Hashtable;

import net.microscraper.client.MissingVariableException;

/**
 * A read-only equivalent to Hashtable<String, String>, implemented by trees.
 * {@link #get} and {@link #containsKey}.  Raises a 
 * {@link MissingVariableException} on missing keys.
 * @author realest
 * @see Hashtable
 *
 */
public interface HasVariableExecutions {
	/**
	 * 
	 * @param key A String key.
	 * @return The String value to which the specified key is mapped.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this map contains no mapping for the key 
	 * @see Hashtable#get
	 */
	public String get(String key) throws MissingVariableException;
	
	/**
	 * Tests if the specified object is a key in this {@link HasVariableExecutions}. 
	 * @param key possible key 
	 * @return <code>true</code> if and only if the specified String is a key
	 * in this {@link HasVariableExecutions}, as determined by the equals method; false otherwise. 
	 * @throws NullPointerException if the key is <code>null</code>
	 * @see Hashtable#containsKey
	 */
	public boolean containsKey(String key);

	public VariableExecution[] getVariableExecutions();

}
