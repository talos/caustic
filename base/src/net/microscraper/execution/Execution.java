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
	 * Run the {@link Execution}.  This must be done before calling any of the methods listed below.
	 * @see #getChildren
	 * @see #isStuck
	 * @see #stuckOn
	 * @see #hasFailed
	 * @see #failedBecause
	 * @see #isComplete
	 * @see #hasPublishName
	 * @see #getPublishName
	 * @see #hasPublishValue
	 * @see #getPublishValue
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
	 * @return An array of {@link Execution}s that this {@link Execution} has created.
	 * @throws IllegalStateException if called before the {@link Execution} is {@link #run}.
	 * @see #run
	 */
	public abstract Execution[] getChildren() throws IllegalStateException;
	
	public abstract boolean isStuck();
	public abstract String stuckOn();
	public abstract boolean hasFailed();
	public abstract Exception failedBecause();
	public abstract boolean isComplete();
	
	public abstract boolean hasPublishName();
	public abstract String getPublishName();
	public abstract boolean hasPublishValue();
	public abstract String getPublishValue();
	
}
