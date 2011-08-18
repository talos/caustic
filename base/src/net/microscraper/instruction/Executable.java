package net.microscraper.instruction;

import net.microscraper.util.Variables;

/**
 * The {@link Executable} interface is implemented by classes
 * that can generate an {@link Execution} from a {@link Variables}.
 * @see Load
 * @see Find
 * @author talos
 *
 */
public interface Executable {
	
	/**
	 * Generate an {@link Execution} bound to <code>source</code> and <code>variables</code>.
	 * @param source The {@link String} to use as the direct source for this
	 * {@link Execution}.
	 * @param variables The {@link Variables} to bind the {@link Execution} to.
	 * @return The bound {@link Execution}.
	 * @throws InterruptedException if the user interrupts during
	 *  {@link #execute(String, Variables)}.
	 */
	public Execution execute(String source, Variables variables) throws InterruptedException;
}
