package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * The {@link MustacheTemplate} name for this {@link Instruction}.  Can be <code>null</code>.
	 * Name should therefore be retrieved through {@link #getName(Variables)}.
	 * @see #getName(Variables, Browser, RegexpCompiler)
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
	 * 
	 * @param shouldSaveValue Whether this {@link Instruction}'s results' values should be
	 * saved. <code>True</code> if they should be, <code>false</code> otherwise.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s results.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * @param finds An array of {@link Instruction}s launched with this {@link Instruction}'s
	 * results.
	 */
	public Instruction(boolean shouldSaveValue,
			MustacheTemplate name, Action action,
			Instruction[] children) {
		this.shouldPersistValue = shouldSaveValue;
		this.name = name;
		this.action = action;
		this.children = children;
	}
	
	public Instruction(Instruction instruction) {
		this.shouldPersistValue = instruction.shouldPersistValue;
		this.name = instruction.name;
		this.action = instruction.action;
		this.children = instruction.children;
	}
	
	/**
	 * @return The raw {@link MustacheTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	/**
	 * Produce an {@link Executable} by binding this {@link Instruction} to a {@link String} source
	 * and {@link Variable}.  The {@link Executable} will reuse these to fire
	 * {@link #execute(String, Variables)} in {@link Executable#execute()}.
	 * @param source The {@link String} source to bind to {@link Executable}.
	 * @param variables The {@link Variables} variables to bind to {@link Executable}.
	 * @return The bound {@link Executable}.
	 */
	/*public Executable bind(String source, Variables variables) {
		return new Executable(source, variables, this);
	}*/
	
	/**
	 * Generate the children of this {@link Instruction} during execution.  There will be as many children
	 * as the product of <code>sources</code> and {@link #children}.  Should be run by
	 * {@link Executable#execute()} as part of {@link InstructionRunner#run()}.
	 * @throws InterruptedException If the user interrupted the execution.
	 * @throws IOException If there was an error persisting to the {@link Database}.
	 * @see Executable#execute()
	 */
	public Execution execute(String source, Variables variables) throws InterruptedException, IOException {
		final Execution result;
		
		Execution nameSub = name.sub(variables);
		
		// Didn't get the name.
		if(!nameSub.isSuccessful()) {
			result = nameSub;
		
		// Got the name.
		} else {
			String name = (String) nameSub.getExecuted();
			
			Execution actionExecution = action.execute(source, variables);
			
			if(!actionExecution.isSuccessful()) {
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
						//		= child.bind(childSource, branch);
					}
				}

				result = Execution.success(childExecutables);
			}
		}
		return result;
	}
}
