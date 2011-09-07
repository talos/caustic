package net.microscraper.instruction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.client.Deserializer;
import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;

/**
 * {@link Instruction}s can be scraped by {@link Scraper#Scraper(Instruction, Database, java.util.Hashtable, String)}.
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
	 * The {@link StringTemplate} name for this {@link Instruction}.
	 */
	private StringTemplate name;
	
	/**
	 * A {@link Vector} of {@link InstructionPromise}s
	 * dependent upon this {@link Instruction}.
	 */
	private final Vector childSerializedInstructions = new Vector();


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
	 * @param name The {@link StringTemplate} name to assign.
	 */
	public void setName(StringTemplate name) {
		this.hasNonDefaultName = true;
		this.name = name;
	}
	
	/**
	 * @return The raw {@link StringTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	/**
	 * Add a {@link SerializedInstruction} that will be used to create {@link Executable}s.
	 * @param child The {@link SerializedInstruction} to add.
	 */
	public void addChild(SerializedInstruction child) {
		childSerializedInstructions.add(child);
	}

	/**
	 * Add an {@link Instruction} that will be used to create {@link Executable}s.
	 * @param child The {@link Instruction} to add.
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
	 * @return An {@link InstructionResult} with {@link Executable}s
	 * if it is successful, or the reasons why the execution did not go off.
	 * @throws InterruptedException If the user interrupted the execution.
	 * @throws IOException If there was an error persisting to the {@link Database}.
	 * @see Executable#execute()
	 */
	public InstructionResult execute(String source, Scope scope)
				throws InterruptedException, IOException {
		final InstructionResult result;
		final String nameStr;
		StringSubstitution nameSub = name.sub(scope);
		// Didn't get the name.
		if(nameSub.isMissingTags()) {
			result = InstructionResult.newMissingTags(nameSub.getMissingTags());
		} else {
			nameStr = nameSub.getSubstituted();
			
			ActionResult actionResult = action.execute(source, scope);
			
			if(actionResult.isMissingTags()) {
				result = InstructionResult.newMissingTags(actionResult.getMissingTags());
			} else {
			
				String[] resultValues = actionResult.getResults();
				int numChildren = childInstructions.size() + childSerializedInstructions.size();
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
					e = childSerializedInstructions.elements();
					while(e.hasMoreElements()) {
						SerializedInstruction serializedInstruction = (SerializedInstruction) e.nextElement();
						childExecutables[childNum] = new Executable(resultValue, childScope, serializedInstruction);
						childNum++;
					}
					e = childInstructions.elements();
					while(e.hasMoreElements()) {
						Instruction instruction = (Instruction) e.nextElement();
						childExecutables[childNum] = new Executable(resultValue, childScope, instruction);
						childNum++;
					}
				}			
	
				result = InstructionResult.newSuccess(childExecutables);
			}
		}
		return result;
	}
}
