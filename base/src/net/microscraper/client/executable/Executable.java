package net.microscraper.client.executable;

import net.microscraper.client.Variables;
import net.microscraper.server.Resource;

/**
 * Implementations of {@link Executable} can be
 * published by a {@link net.microscraper.client.interfaces.Publisher}.
 * @author john
 *
 */
public interface Executable extends Runnable {
	
	/**
	 * 
	 * @return A unique ID for this Execution.
	 */
	public abstract int getId();
	
	/**
	 * 
	 * Run the {@link Executable}.
	 * @see #isComplete()
	 * @see #isStuck()
	 * @see #hasFailed()
	 */
	public abstract void run();
	
	/**
	 * Can be called before the {@link Executable} is {@link #run}.
	 * @return The {@link Resource} with the {@link Executable}'s instructions.
	 */
	public abstract Resource getResource();
	
	/**
	 * Can be called before the {@link Executable} is {@link #run}.
	 * @return Whether this {@link Executable} was created by another {@link Executable}.
	 * @see getParent()
	 */
	public abstract boolean hasParent();
	
	/**
	 * Can be called before the {@link Executable} is {@link #run}.
	 * @return The {@link Executable} that created this {@link Executable}.
	 * @throws NullPointerException If the {@link Executable} has no parent.
	 * @see hasParent()
	 */
	public abstract Executable getParent() throws NullPointerException;
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has not {@link #run} successfully, and will not do so
	 * without an update to its {@link Variables}, <code>false</code> otherwise.
	 * @see #run()
	 * @see #stuckOn()
	 */
	public abstract boolean isStuck();

	/**
	 * 
	 * @return The name of the {@link MissingVariable} that is stopping this {@link Executable} 
	 * from completing its {@link #run}, if the {@link #isStuck} is returning <code>true</code>.
	 * @throws IllegalStateException if called when {@link #isStuck} is not <code>true</code>.
	 * @see #isStuck()
	 */
	public abstract String stuckOn() throws IllegalStateException;
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has definitevely failed to {@link #run},
	 * and cannot do so, <code>false</code> otherwise.
	 * @see #run()
	 * @see #failedBecause()
	 */
	public abstract boolean hasFailed();
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has definitevely failed to {@link #run},
	 * and cannot do so.
	 * @see #run()
	 * @see #hasFailed()
	 */
	public abstract Throwable failedBecause() throws IllegalStateException;
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has {@link #run} successfully, and can have
	 * {@link #getChildren} called upon it, <code>false</code> otherwise.
	 * @see #run()
	 * @see #getChildren()
	 * @see #getValue()
	 */
	public abstract boolean isComplete();
	

	/**
	 * 
	 * @return An array of {@link Executable}s that this {@link Executable} has created.
	 * @throws IllegalStateException if called before the {@link Executable} is {@link #run}.
	 * @see #run()
	 * @see #isComplete()
	 */
	public abstract Executable[] getChildren() throws IllegalStateException;
	

	/**
	 * 
	 * @return The {@link Variables} instance accessible inside this {@Executable}.
	 */
	public abstract Variables getVariables() throws IllegalStateException;
	
	
	/**
	 * 
	 * @return <code>True</code> if this {@link Executable} is identified by a particular name for the
	 * {@link Publisher}, <code>false</code> otherwise.
	 * @see #getPublishName()
	 */
	public abstract boolean hasName();
	
	/**
	 * 
	 * @return A particular string for the {@link Publisher} use as a name for this {@link Executable}.
	 * @throw {@link NullPointerException} if {@link #hasName()} is <code>false</code>
	 * @see #hasName()
	 */
	public abstract String getName() throws NullPointerException;
	
	
	/**
	 * 
	 * @return <code>True</code> if this {@link Executable} produces a value that {@link Publisher} can use,
	 * <code>false</code> otherwise.
	 * @see #getValue()
	 */
	public abstract boolean hasValue();
	
	/**
	 * 
	 * @return The value that {@link Publisher} should use for this {@link Executable}.
	 * @throws NullPointerException If {@link #hasPublishValue} is <code>false</code>.
	 * @throws IllegalStateException If {@link #isComplete} is <code>false</code>.
	 * @see #hasValue()
	 * @see #isComplete()
	 */
	public abstract String getValue() throws NullPointerException, IllegalStateException;
	
}
