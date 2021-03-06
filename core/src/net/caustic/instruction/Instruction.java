package net.caustic.instruction;

import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;

public abstract class Instruction {
	
	private final StringTemplate name;
	
	/**
	 * {@link Vector} of {@link Instruction}s to fire when this one is done.
	 */
	private final Vector children = new Vector();
	
	Instruction() {
		this.name = null;
	}
	
	Instruction(StringTemplate name) {
		this.name = name;
	}
	
	Instruction[] getChildren() {
		Instruction[] result = new Instruction[children.size()];
		children.copyInto(result);
		return result;
	}
	
	/**
	 * 
	 * @return The {@link StringTemplate} name of the {@link Instruction}.  Is
	 * <code>null</code> if it has no name.
	 */
	public StringTemplate getName() {
		return name;
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
	 * @return <code>true</code> if this {@link Instruction} is an instruction
	 * that should only be executed with confirmation, <code>false</code>
	 * if this {@link Instruction} can be executed without confirmation.
	 */
	public abstract boolean shouldConfirm();
	
	/**
	 * 
	 * @param source The {@link String} to use as the source
	 * for this {@link Instruction}.
	 * @param db The {@link Database} to use as input
	 * for template substitutions.
	 * @param scope The {@link Scope} within <code>db</code>
	 * @return A {@link InstructionResult} object with either successful
	 * values and children, or information about why
	 * this method did not work.
	 * @throws InterruptedException if the user interrupted during
	 * the method.
	 * @throws DatabaseException if there was an error persisting to 
	 * or reading from <code>view</code>.
	 */
	public abstract InstructionResult execute(String source, Database db, Scope scope, HttpBrowser browser) throws InterruptedException, DatabaseException;
}
