package net.microscraper.instruction;

import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link Action} is the section of an {@link Instruction} that produces an 
 * {@link Execution} whose {@link Execution#getExecuted()} is a {@link String} array
 * from an execution-time set of {@link Variable}s and an
 * execution-time {@link String} source.
 * @author talos
 *
 */
public interface Action {
	
	/**
	 * Execute the action using a {@link String} source and {@link Variables}.
	 * @param source The {@link String} source.
	 * @param variables The {@link Variables} to use when executing.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a
	 * {@link String} array.
	 * @throws InterruptedException If the user interrupts the action.
	 */
	public Execution execute(String source, Variables variables) throws InterruptedException;
}
