package net.microscraper.execution;

import java.net.URI;

/**
 * Implementations of Execution can be executed by a {@link net.microscraper.client.Client},
 * and published by {@link net.microscraper.client.Publisher}.
 * @author john
 *
 */
public interface Execution extends Runnable {
	
	/**
	 * 
	 * @return A unique ID for this Execution.
	 */
	public abstract int getId();
	
	/**
	 * 
	 * Run the {@link Execution}.
	 * @see #isComplete
	 * @see #isStuck
	 * @see #hasFailed
	 */
	public abstract void run();
	
	/**
	 * Can be called before the {@link Execution} is {@link #run}.
	 * @return A {@link URI} identifying where the {@link Resource} with the execution's instructions
	 * is located. 
	 */
	public abstract URI getResourceLocation();
	
	/**
	 * Can be called before the {@link Execution} is {@link #run}.
	 * @return Whether this {@link Execution} was created by another {@link Execution}.
	 * @see getParent
	 */
	public abstract boolean hasParent();
	
	/**
	 * Can be called before the {@link Execution} is {@link #run}.
	 * @return The {@link Execution} that created this {@link Execution}.
	 * @see hasParent
	 * @throws NullPointerException If the {@link Execution} has no parent.
	 */
	public abstract Execution getParent() throws NullPointerException;
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has not {@link #run} successfully, and will not do so
	 * without an update to its {@link Variables}, <code>false</code> otherwise.
	 * @see #run
	 * @see #stuckOn
	 */
	public abstract boolean isStuck();

	/**
	 * 
	 * @return The name of the {@link MissingVariable} that is stopping this {@link Execution} 
	 * from completing its {@link #run}, if the {@link #isStuck} is returning <code>true</code>.
	 * @throws IllegalStateException if called when {@link #isStuck} is not <code>true</code>.
	 * @see #isStuck
	 */
	public abstract String stuckOn() throws IllegalStateException;
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has definitevely failed to {@link #run},
	 * and cannot do so, <code>false</code> otherwise.
	 * @see #run
	 * @see #failedBecause
	 */
	public abstract boolean hasFailed();
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has definitevely failed to {@link #run},
	 * and cannot do so.
	 * @see #run
	 * @see #failedBecause
	 */
	public abstract Throwable failedBecause() throws IllegalStateException;
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has {@link #run} successfully, and can have
	 * {@link #getChildren} called upon it, <code>false</code> otherwise.
	 * @see #run
	 * @see #getChildren
	 * @see #getPublishValue
	 */
	public abstract boolean isComplete();
	

	/**
	 * 
	 * @return An array of {@link Execution}s that this {@link Execution} has created.
	 * @throws IllegalStateException if called before the {@link Execution} is {@link #run}.
	 * @see #run
	 * @see #isComplete
	 */
	public abstract Execution[] getChildren() throws IllegalStateException;
	
	
	/**
	 * 
	 * @return <code>True</code> if this {@link Execution} is identified by a particular name for the
	 * {@link Publisher}, <code>false</code> otherwise.
	 * @see #getPublishName
	 */
	public abstract boolean hasPublishName();
	
	/**
	 * 
	 * @return A particular string for the {@link Publisher} use as a name for this {@link Execution}.
	 * @see #hasPublishName
	 * @throw {@link NullPointerException} if {@link #hasPublishName()} is <code>false</code>
	 */
	public abstract String getPublishName() throws NullPointerException;
	
	
	/**
	 * 
	 * @return <code>True</code> if this {@link Execution} produces a value that {@link Publisher} can use,
	 * <code>false</code> otherwise.
	 * @see #getPublishValue
	 */
	public abstract boolean hasPublishValue();
	
	/**
	 * 
	 * @return The value that {@link Publisher} should use for this {@link Execution}.
	 * @throws NullPointerException If {@link #hasPublishValue} is <code>false</code>.
	 * @throws IllegalStateException If {@link #isComplete} is <code>false</code>.
	 * @see #hasPublishValue()
	 * @see #isComplete()
	 */
	public abstract String getPublishValue() throws NullPointerException, IllegalStateException;
	
}
