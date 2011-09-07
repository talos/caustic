package net.microscraper.instruction;

import net.microscraper.database.Scope;
import net.microscraper.template.StringTemplate;

/**
 * An {@link Action} is the section of an {@link Instruction} that produces an 
 * {@link ActionResult} whose {@link ActionResult#getResults()} is a {@link String} array
 * from an execution-time set of {@link Variable}s and an
 * execution-time {@link String} source.
 * @author talos
 *
 */
public interface Action {
	
	/**
	 * Execute the action using a {@link String} source and {@link Scope}.
	 * @param source The {@link String} source.
	 * @param scope The {@link Scope} to use when extracting values from {@link Database}.
	 * @return An {@link ActionResult}.
	 * @throws InterruptedException If the user interrupts the action.
	 */
	public ActionResult execute(String source, Scope scope) throws InterruptedException;
	
	/**
	 * A default name for this {@link Action}'s results when one is not specified.
	 * @return A {@link StringTemplate} default name.
	 */
	public StringTemplate getDefaultName();
}
