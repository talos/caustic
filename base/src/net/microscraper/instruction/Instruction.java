package net.microscraper.instruction;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.template.Template;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * Whether this {@link Instruction} has a particular name.
	 */
	private boolean hasName;
	
	/**
	 * The {@link Template} name for this {@link Instruction}.
	 */
	private Template name;
	
	/**
	 * A {@link Vector} of {@link InstructionPromise}s dependent upon this {@link Instruction}.
	 */
	private final Vector children = new Vector();
	
	/**
	 * Whether or not this {@link Instruction} should save the values of its results to the {@link Database}.
	 */
	private boolean shouldPersistValue;
	
	/**
	 * The {@link Action} done by this {@link Instruction}.
	 */
	private final Action action;

	/**
	 * Create a new {@link Instruction}.
	 * Uses {@link Action#getDefaultShouldPersistValue()} for {@link #shouldPersistValue} and
	 * {@link Action#getDefaultName()} for {@link #name}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action) {
		this.shouldPersistValue = action.getDefaultShouldPersistValue();
		this.action = action;
		this.hasName = false;
		this.name = action.getDefaultName();
	}
	
	/**
	 * Assign a {@link #name} to this {@link Instruction}.
	 * @param name The {@link Template} name to assign.
	 */
	public void setName(Template name) {
		this.hasName = true;
		this.name = name;
	}

	/**
	 * Assign a {@link #shouldPersistValue} to this {@link Instruction}.
	 * @param name The {@link boolean} value to assign.
	 */
	public void setShouldPersistValue(boolean shouldPersistValue) {
		this.shouldPersistValue = shouldPersistValue;
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
		
		Execution nameSub = name.sub(variables);
		
		// Didn't get the name.
		if(nameSub.isSuccessful() == false) {
			result = nameSub;
		
		// Got the name.
		} else {
			String name = (String) nameSub.getExecuted();
			
			Execution actionExecution = action.execute(source, variables);
			
			if(actionExecution.isSuccessful() == false) {
				result = actionExecution;
			} else {
			
				String[] resultValues = (String[]) actionExecution.getExecuted();
				Executable[] childExecutables = new Executable[resultValues.length * children.size()];
				
				// Generate new Variables instances for the kids.
				Variables[] branches;
				
				if(resultValues.length == 1) {
					branches = new Variables[] { Variables.singleBranch(variables, name, resultValues[0], hasName, shouldPersistValue) };	
				} else {
					branches = Variables.multiBranch(variables, name, resultValues, hasName, shouldPersistValue);						
				}
				
				for(int i = 0 ; i < resultValues.length ; i ++) {
					Variables branch = branches[i];
					String childSource = resultValues[i];
					for(int j = 0 ; j < children.size() ; j++) {
						InstructionPromise child = (InstructionPromise) children.elementAt(j);
						childExecutables[(i * children.size()) + j]
								= new Executable(childSource, branch, child);
					}
				}

				result = Execution.success(childExecutables);
			}
		}
		return result;
	}
}
