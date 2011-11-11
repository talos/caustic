package net.microscraper.instruction;

import java.util.Vector;

import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;

public abstract class Instruction {

	/**
	 * {@link Vector} of {@link Instruction}s to fire when this one is done.
	 */
	private final Vector children = new Vector();
	
	protected Instruction[] getChildren() {
		Instruction[] result = new Instruction[children.size()];
		children.copyInto(result);
		return result;
	}
	
	/**
	 * Add an {@link Instruction} to fire upon this {@link Instruction}'s completion.
	 * @param instruction The {@link Instruction} to fire.  Will append to existing
	 * children.
	 */
	public void then(Instruction instruction) {
		children.addElement(instruction);
	}
	
	/**
	 * 
	 * @param source The {@link String} to use as the source
	 * for this {@link Instruction}.
	 * @param view The {@link DatabaseView} to use as input
	 * for template substitutions.
	 * @return A {@link InstructionResult} object with either successful
	 * values and children, or information about why
	 * this method did not work.
	 * @throws InterruptedException if the user interrupted during
	 * the method.
	 * @throws DatabaseException if there was an error persisting to 
	 * or reading from <code>view</code>.
	 */
	public abstract InstructionResult execute(String source, DatabaseView view, HttpBrowser browser) throws InterruptedException, DatabaseException;
}
