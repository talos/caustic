package net.microscraper.instruction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.database.Variables;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * Whether this {@link Instruction} has a particular name.
	 */
	private boolean hasNonDefaultName = false;
	
	/**
	 * The {@link Template} name for this {@link Instruction}.
	 */
	private Template name;
	
	/**
	 * A {@link Vector} of {@link InstructionPromise}s dependent upon this {@link Instruction}.
	 */
	private final Vector children = new Vector();
	
	/**
	 * The {@link Action} done by this {@link Instruction}.
	 */
	private final Action action;

	/**
	 * Create a new {@link Instruction}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action) {
		this.action = action;
		this.name = action.getDefaultName();
	}
	
	/**
	 * Assign a {@link #name} to this {@link Instruction}.
	 * @param name The {@link Template} name to assign.
	 */
	public void setName(Template name) {
		this.hasNonDefaultName = true;
		this.name = name;
	}
	
	/**
	 * @return The raw {@link Template} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	/**
	 * Add a {@link InstructionPromise} that will be used to create {@link Executable}s
	 * upon {@link #execute(String, Variables)}.
	 * @param child The {@link InstructionPromise} to add.
	 */
	public void addChild(InstructionPromise child) {
		children.add(child);
	}
	
	/**
	 * Generate the {@link Executable} children of this
	 * {@link Instruction} during execution.  There will be as many children
	 * as the product of {@link Action#execute(String, Variables)}'s {@link Execution#getExecuted()}
	 *  and {@link #children}.  Should be run by {@link Executable#execute()} as part of {@link InstructionRunner#run()}.
	 * @param source The source {@link String} to use in the execution.
	 * @param variables The {@link Variables} to use in the execution.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is an array of {@link Executable}s,
	 * if it is successful, or the reasons why the execution did not go off.
	 * @throws InterruptedException If the user interrupted the execution.
	 * @throws IOException If there was an error persisting to the {@link Database}.
	 * @see Executable#execute()
	 */
	public Execution execute(String source, Variables variables) throws InterruptedException, IOException {
		final Execution result;
		final String nameStr;
		Execution nameSub = name.sub(variables);
		// Didn't get the name.
		if(nameSub.isSuccessful() == false) {
			return nameSub; // eject
		}
		nameStr = (String) nameSub.getExecuted();

		Execution actionExecution = action.execute(source, variables);
		
		if(actionExecution.isSuccessful() == false) {
			result = actionExecution;
		} else {
		
			String[] resultValues = (String[]) actionExecution.getExecuted();
			Executable[] childExecutables = new Executable[resultValues.length * children.size()];
			for(int i = 0 ; i < resultValues.length ; i ++ ) {
				final String resultValue = resultValues[i];
				final Variables childVariables;
				
				// Save the value to Variables (and the database).
				if(resultValues.length == 1) {
					if(hasNonDefaultName) {
						childVariables = variables.storeOneToOne(nameStr, resultValue);
					} else {
						childVariables = variables.storeOneToOne(nameStr);
					}
				} else {
					if(hasNonDefaultName) {
						childVariables = variables.storeOneToMany(nameStr, resultValue);
					} else {
						childVariables = variables.storeOneToMany(nameStr);
					}
				}
				
				// Generate children.
				int instructionPromiseNum = i * children.size();
				Enumeration e = children.elements();
				while(e.hasMoreElements()) {
					InstructionPromise promise = (InstructionPromise) e.nextElement();
					childExecutables[instructionPromiseNum] = new Executable(resultValue, childVariables, promise);
					instructionPromiseNum++;
				}
			}

			result = Execution.success(childExecutables);
		}
		
		return result;
	}
}
