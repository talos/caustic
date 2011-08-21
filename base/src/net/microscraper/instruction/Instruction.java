package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * The {@link MustacheTemplate} name for this {@link Instruction}.
	 */
	private final MustacheTemplate name;
	
	/**
	 * An array of {@link Instruction}s dependent upon this {@link Instruction}.
	 */
	private final Instruction[] children;
	
	/**
	 * Whether or not this {@link Instruction} should save the values of its results to the {@link Database}.
	 */
	private final boolean shouldPersistValue;
	
	/**
	 * The {@link Action} done by this {@link Instruction}.
	 */
	private final Action action;
	


	/**
	 * Create a new {@link Instruction}.
	 * Uses {@link Action#getDefaultShouldPersistValue()} for {@link #shouldPersistValue} and
	 * {@link Action#getDefaultName()} for {@link #name}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * @param children An array of {@link Instruction}s launched with this {@link Instruction}'s
	 * results.
	 * @param shouldPersistValue Whether this {@link Instruction}'s executions' values should be
	 * persisted to {@link Database}. <code>True</code> if they should be, <code>false</code> otherwise.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action, Instruction[] children) {
		this.shouldPersistValue = action.getDefaultShouldPersistValue();
		this.action = action;
		this.children = children;
		this.name = action.getDefaultName();
	}
	
	
	/**
	 * Create a new {@link Instruction} with a custom value for {@link #name}.
	 * Uses {@link Action#getDefaultShouldPersistValue()} for {@link #shouldPersistValue}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * @param children An array of {@link Instruction}s launched with this {@link Instruction}'s
	 * results.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action,
			Instruction[] children, MustacheTemplate name) {
		this.shouldPersistValue = action.getDefaultShouldPersistValue();
		this.action = action;
		this.children = children;
		this.name = name;
	}
	
	/**
	 * Create a new {@link Instruction} with a custom value for {@link #name}.
	 * Uses {@link Action#getDefaultName()} for {@link #name}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * @param children An array of {@link Instruction}s launched with this {@link Instruction}'s
	 * results.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action,
			Instruction[] children, boolean shouldPersistValue) {
		this.shouldPersistValue = shouldPersistValue;
		this.action = action;
		this.children = children;
		this.name = action.getDefaultName();
	}
	
	/**
	 * Create a new {@link Instruction} with a custom value for {@link #shouldPersistValue}
	 * and {@link #name}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * @param children An array of {@link Instruction}s launched with this {@link Instruction}'s
	 * results.
	 * @param shouldPersistValue Whether this {@link Instruction}'s executions' values should be
	 * persisted to {@link Database}. <code>True</code> if they should be, <code>false</code> otherwise.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s results.
	 */
	public Instruction(Action action,
			Instruction[] children, boolean shouldPersistValue,
			MustacheTemplate name) {
		this.shouldPersistValue = shouldPersistValue;
		this.name = name;
		this.action = action;
		this.children = children;
	}
	
	/**
	 * @return The raw {@link MustacheTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	/**
	 * Generate the children of this {@link Instruction} during execution.  There will be as many children
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
				Executable[] childExecutables = new Executable[resultValues.length * children.length];
				
				// Generate new Variables instances for the kids.
				Variables[] branches = Variables.branch(variables, name, resultValues, shouldPersistValue);
				for(int i = 0 ; i < resultValues.length ; i ++) {
					Variables branch = branches[i];
					String childSource = resultValues[i];
					for(int j = 0 ; j < children.length ; j++) {
						Instruction child = children[j];
						childExecutables[i * resultValues.length + j]
								= new Executable(childSource, branch, child);
					}
				}

				result = Execution.success(childExecutables);
			}
		}
		return result;
	}
}
