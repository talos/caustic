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
	 * @return The {@link Result} that spawned this {@link Executable}.
	 * @see {@link #hasSource()}
	 */
	public abstract Result getSource();

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
	 * @throws IllegalStateException If called when {@link #isStuck} is not <code>true</code>.
	 * @see #isStuck()
	 */
	public abstract String stuckOn() throws IllegalStateException;
	

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has failed to {@link #run},
	 * and cannot do so, <code>false</code> otherwise.
	 * @see #run()
	 * @see #failedBecause()
	 */
	public abstract boolean hasFailed();
	

	/**
	 * 
	 * @return The {@link Throwable} that caused the {@link Executable} to fail to {@link #run},
	 * @throws IllegalStateException If {@link #hasFailed} is <code>false</code>.
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
	 * @see #getResult()
	 */
	public abstract boolean isComplete();
	

	/**
	 * 
	 * @return An array of {@link Result}s generated from a successful {@link #run}.
	 * May be a 0-length array.
	 * @throws IllegalStateException If {@link #isComplete} is <code>false</code>.
	 * @see {@link #isComplete()}
	 */
	public abstract Result[] getResults() throws IllegalStateException;
	
	/**
	 * 
	 * @return An array of fresh {@link Executable}s that this {@link Executable} has created.
	 * @throws IllegalStateException if called before the {@link Executable} {@link #isComplete()}.
	 * @see #run()
	 * @see #isComplete()
	 */
	public abstract Executable[] getChildren() throws IllegalStateException;
	

	/**
	 * 
	 * @return The {@link Variables} instance accessible inside this {@link Executable}.
	 */
	public abstract Variables getVariables();
	
	/**
	 * 
	 * @return A unique identifier for this {@link Executable}.
	 */
	public abstract int getId();
	
	/**
	 * 
	 * @return Whether this {@link Executable} has a special name for {@link Publisher}.
	 * @see #getName()
	 */
	//public abstract boolean hasName();
	

	/**
	 * 
	 * @return This {@link Executable}'s special name for {@link Publisher}.
	 * @see #hasName()
	 * @throws IllegalStateException if called before the {@link Executable} {@link #isComplete()}.
	 */
	//public abstract String getName();
	
	/**
	 * 
	 * @return Whether this {@link Executable} has a value for {@link Publisher}.
	 * @see #getValue()
	 */
	//public abstract boolean hasValue();
	

	/**
	 * 
	 * @return Whether this {@link Executable} has a value for {@link Publisher}.
	 * @throws NullPointerException if {@link hasValue()} is <code>false</code>
	 * @throws IllegalStateException if called before the {@link Executable} {@link #isComplete()}.
	 * @see #hasValue()
	 */
	//public abstract String getValue() throws IllegalStateException;
}
