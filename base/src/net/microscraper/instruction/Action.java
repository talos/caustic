package net.microscraper.instruction;

import net.microscraper.database.Scope;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;

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
	 * @param scope The {@link Scope} to use when extracting values from {@link Database}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a
	 * {@link String} array.
	 * @throws InterruptedException If the user interrupts the action.
	 */
	public Execution execute(String source, Scope scope) throws InterruptedException;
	
	/**
	 * A default name for this {@link Action}'s results when one is not specified.
	 * @return A {@link Template} default name.
	 */
	public Template getDefaultName();
}
