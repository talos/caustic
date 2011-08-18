package net.microscraper.instruction;

import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Variables;

/**
 * When successful, an {@link Execution} generates {@link Result}s.  It is
 * bound to a {@link Variables} instance.  If the {@link Variables} instance
 * is currently missing a variable it needs to run, it can explain what it is missing.
 * If it is missing the same variable after to consecutive tries, it considers itself
 * stuck.  If it fails due to some exception, it can explain what.
 * @author john
 *
 */
public final class Execution {
	
	private final Executable executable;
	private final Result source;
	private final Variables variables;
	
	private Result[] results = null;
	//private Execution[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	/**
	 * Search for <code>key</code> only within this {@link Execution} or its
	 * immediate descendents.
	 * @param key the {@link String} to retrieve.
	 * @return the value mapped to {@link String}, or <code>null</code> if it
	 * is not mapped.
	 */
	/*private String localGet(String key) {
		if(source != null) {
			if(source.getName().equals(key)) {
				return source.getValue();
			}
		}
		if(results != null) {
			if(results.length == 1) {
				if(results[0].getName().equals(key)) {
					return results[0].getValue();
				}
			}
		}
		if(children != null) {
			for(int i = 0 ; i < children.length ; i++) {
				String localValue = children[i].localGet(key);
				if(localValue != null) {
					return localValue;
				}
			}
		}
		return null;
	}*/
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has not {@link #run} successfully, and will not do so
	 * without an update to its {@link Variables}, <code>false</code> otherwise.
	 * @see #run()
	 * @see #stuckOn()
	 */
	public final boolean isStuck() {
		return isStuck;
	}

	/**
	 * 
	 * @return The name of the {@link MissingVariable} that is stopping this {@link Execution} 
	 * from completing its {@link #run}, if the {@link #isStuck} is returning <code>true</code>.
	 * @throws IllegalStateException If called when {@link #isStuck} is not <code>true</code>.
	 * @see #isStuck()
	 */
	public final String stuckOn() throws IllegalStateException {
		if(isStuck()) {
			return missingVariable;
		} else {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has failed to {@link #run},
	 * and cannot do so, <code>false</code> otherwise.
	 * @see #run()
	 * @see #failedBecause()
	 */
	public final boolean hasFailed() {
		if(failure != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @return The {@link Throwable} that caused the {@link Execution} to fail to {@link #run},
	 * @throws IllegalStateException If {@link #hasFailed} is <code>false</code>.
	 * @see #run()
	 * @see #hasFailed()
	 */
	public final Throwable failedBecause() throws IllegalStateException {
		if(hasFailed()) {
			return failure;
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has {@link #run} successfully, and can have
	 * {@link #getChildren} called upon it, <code>false</code> otherwise.
	 * @see #run()
	 * @see #getChildren()
	 * @see #getResult()
	 */
	public final boolean isComplete() {
		return isComplete;
	}
	
	
	/**
	 * Returns {@link #instruction} as  {@link String}.
	 */
	public final String toString() {
		return instruction.toString();
	}
	
	/**
	 * 
	 * @return An array of fresh {@link Execution}s that this {@link Execution} has created.
	 * @throws IllegalStateException if called before the {@link Execution} {@link #isComplete()}.
	 * @see #run()
	 * @see #isComplete()
	 */
	public final Execution[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
	
	public String get(String key) {
		String localValue = localGet(key);
		if(localValue != null) {
			return localValue;
		} else {
			return variables.get(key); // search up.
		}
	}

	public boolean containsKey(String key) {
		if(get(key) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Attempt to run the {@link Execution}.
	 */
	public void run() {
		
	}
}
