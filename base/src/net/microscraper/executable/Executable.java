package net.microscraper.executable;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Instruction;

/**
 * Implementations of {@link Executable} can be
 * published by a {@link net.microscraper.interfaces.database.Database}.
 * @author john
 *
 */
public interface Executable extends Runnable, Variables {

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
	 * @return The {@link Instruction} with the {@link Executable}'s instructions.
	 */
	public abstract Instruction getInstruction();
	
	/**
	 * Can be called before the {@link Executable} is {@link #run}.
	 * @return Whether this {@link Executable} was spawned by a {@link Result}.
	 * @see {@link #getSource()}
	 */
	public abstract boolean hasSource();
	
	/**
	 * Can be called before the {@link Executable} is {@link #run}.
	 * @return The {@link Result} that spawned this {@link Executable}.
	 * @see {@link #hasSource()}
	 * @throws NullPointerException if {@link #hasSource()} is <code>false</code>.
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

	public abstract String getName() throws MissingVariableException,
			MustacheTemplateException;

}
