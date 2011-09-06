package net.microscraper.instruction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.template.Template;
import net.microscraper.util.Execution;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * The {@link Database} to store results to.
	 */
	private Database database;
	
	/**
	 * Whether this {@link Instruction} has a particular name.
	 */
	private boolean hasNonDefaultName = false;
	
	/**
	 * The {@link Template} name for this {@link Instruction}.
	 */
	private Template name;
	
	/**
	 * A {@link Vector} of {@link InstructionPromise}s
	 * dependent upon this {@link Instruction}.
	 */
	private final Vector childPromises = new Vector();


	/**
	 * A {@link Vector} of {@link Instruction}s
	 * dependent upon this {@link Instruction}.
	 */
	private final Vector childInstructions = new Vector();

	
	/**
	 * The {@link Action} done by this {@link Instruction}.
	 */
	private final Action action;

	/**
	 * Create a new {@link Instruction}.
	 * @param action The {@link Action} that produces this {@link Instruction}'s results.
	 * {@link Instruction}'s results.
	 * @param database The {@link Database} to store results to.
	 */
	public Instruction(Action action, Database database) {
		this.action = action;
		this.database = database;
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
		childPromises.add(child);
	}

	/**
	 * Add an {@link Instruction} that will be used to create {@link Executable}s
	 * upon {@link #execute(String, Variables)}.
	 * @param child The {@link InstructionPromise} to add.
	 */
	public void addChild(Instruction child) {
		childInstructions.add(child);
	}
	
	/**
	 * Generate the {@link Executable} children of this
	 * {@link Instruction} during execution.  There will be as many children
	 * as the product of {@link Action#execute(String, Scope)}'s {@link Execution#getExecuted()}
	 *  and {@link #children}.  Should be run by {@link Executable#execute()} as part of {@link Scraper#run()}.
	 * @param source The source {@link String} to use in the execution.
	 * @param scope The {@link Scope} to use when substituting from a {@link Database}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is an array of {@link Executable}s,
	 * if it is successful, or the reasons why the execution did not go off.
	 * @throws InterruptedException If the user interrupted the execution.
	 * @throws IOException If there was an error persisting to the {@link Database}.
	 * @see Executable#execute()
	 */
	public Execution execute(String source, Scope scope)
				throws InterruptedException, IOException {
		final Execution result;
		final String nameStr;
		Execution nameSub = name.sub(scope);
		// Didn't get the name.
		if(nameSub.isSuccessful() == false) {
			return nameSub; // eject
		}
		nameStr = (String) nameSub.getExecuted();
		
		Execution actionExecution = action.execute(source, scope);
		
		if(actionExecution.isSuccessful() == false) {
			result = actionExecution;
		} else {
		
			String[] resultValues = (String[]) actionExecution.getExecuted();
			int numChildren = childInstructions.size() + childPromises.size();
			Executable[] childExecutables =
					new Executable[resultValues.length * numChildren];
			for(int i = 0 ; i < resultValues.length ; i ++ ) {
				final String resultValue = resultValues[i];
				final Scope childScope;
				
				// Save the value to Variables (and the database).
				if(resultValues.length == 1) {
					if(hasNonDefaultName) {
						database.storeOneToOne(scope, nameStr, resultValue);
					} else {
						database.storeOneToOne(scope, nameStr);
					}
					childScope = scope;
				} else {
					if(hasNonDefaultName) {
						childScope = database.storeOneToMany(scope, nameStr, resultValue);
					} else {
						childScope = database.storeOneToMany(scope, nameStr);
					}
				}
				
				// Generate children from both promises and real instructions.
				int childNum = i * numChildren;
				Enumeration e;
				e = childPromises.elements();
				while(e.hasMoreElements()) {
					InstructionPromise promise = (InstructionPromise) e.nextElement();
					childExecutables[childNum] = new Executable(resultValue, childScope, promise);
					childNum++;
				}
				e = childInstructions.elements();
				while(e.hasMoreElements()) {
					Instruction instruction = (Instruction) e.nextElement();
					childExecutables[childNum] = new Executable(resultValue, childScope, instruction);
					childNum++;
				}
			}			

			result = Execution.success(childExecutables);
		}
		
		return result;
	}
}
