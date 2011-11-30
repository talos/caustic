package net.caustic.instruction;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.scope.Scope;

public interface Instruction {
	
	/**
	 * 
	 * @return <code>true</code> if this {@link Instruction} is an instruction
	 * that should only be executed with confirmation, <code>false</code>
	 * if this {@link Instruction} can be executed without confirmation.
	 */
	public abstract boolean shouldConfirm();
	
	/**
	 * 
	 * @param source The {@link String} to use as the source
	 * for this {@link Instruction}.  Can be <code>null</code>.
	 * @param db The {@link Database} to use as input
	 * for template substitutions.
	 * @param scope The {@link Scope} within <code>db</code> to use
	 * for substitution.
	 * @param browser An {@link HttpBrowser} to use.
	 * @return A {@link InstructionResult} object with either successful
	 * values and children, or information about why
	 * this method did not work.
	 * @throws InterruptedException if the user interrupted during
	 * the method.
	 * @throws DatabaseException if there was an error persisting to 
	 * or reading from <code>db</code>.
	 */
	public abstract void execute(String source, Database db, Scope scope, HttpBrowser browser)
			throws InterruptedException, DatabaseException;
}
