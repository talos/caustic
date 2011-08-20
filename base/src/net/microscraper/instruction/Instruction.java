package net.microscraper.instruction;

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
public class Instruction  {
	
	/**
	 * The {@link MustacheTemplate} name for this {@link Instruction}.  Can be <code>null</code>.
	 * Name should therefore be retrieved through {@link #getName(Variables)}.
	 * @see #getName(Variables, Browser, RegexpCompiler)
	 */
	private final MustacheTemplate name;
	
	/**
	 * An array of {@link Find}s dependent upon this {@link Instruction}.
	 */
	//private final Find[] finds;
	
	/**
	 * An array of {@link Load}s dependent upon this {@link Instruction}.
	 */
	//private final Load[] loads;
	
	/**
	 * An array of {@link Instruction}s dependent upon this {@link Instruction}.
	 */
	private final Instruction[] children;
	
	/**
	 * Whether or not this {@link Instruction} should save the values of its results.
	 */
	private final boolean shouldSaveValue;
	
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
		this.shouldSaveValue = shouldSaveValue;
		this.name = name;
		this.action = action;
		this.children = children;
	}
	
	public Instruction(Instruction instruction) {
		this.shouldSaveValue = instruction.shouldSaveValue;
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
	
	public Executable bind(String source, Variables variables) {
		return new Executable(source, variables, this);
	}
	
	/**
	 * Generate the children of this {@link Instruction} during execution.  There will be as many children
	 * as the product of <code>sources</code> and {@link #children}.
	 */
	public Execution execute(String source, Variables variables) throws InterruptedException {
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
				
				// Only one resultValue, modifies the Variables (passes up
				// the new value).
				if(resultValues.length == 1) {
					variables.put(name, resultValues[0]);
					for(int i = 0 ; i < resultValues.length ; i++) {
						childExecutables[i] = children[i].bind(resultValues[i], variables);
					}
				} else {
					
					// Multiple resultValues, copies but does not modify the Variables.
					for(int i = 0 ; i < resultValues.length ; i ++) {
						Variables branchedVariables
							= Variables.branch(variables, name, resultValues[i]);
						for(int j = 0 ; j < resultValues.length ; j++) {
							childExecutables[i * children.length + j]
									= children[i].bind(resultValues[j], branchedVariables);
						}

					}
				}
				result = Execution.success(childExecutables);
			}
		}
		return result;
	}
}
