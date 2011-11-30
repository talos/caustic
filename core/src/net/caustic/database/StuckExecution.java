package net.caustic.database;

import java.util.Vector;

import net.caustic.instruction.Instruction;
import net.caustic.util.VectorUtils;

class StuckExecution extends ReadyExecution {

	private final Vector missingTags = new Vector();
	private ReadyExecution ready;
	
	StuckExecution(String source, Instruction instruction, String[] missingTags) {
		super(source, instruction);
		VectorUtils.arrayIntoVector(missingTags, this.missingTags);
		ready = new ReadyExecution(source, instruction);
	}
	
	/**
	 * Inform {@link StuckExecution} that a tag with name <code>name</code>
	 * has appeared in a scope that affects it.
	 * @param name The {@link String} name of the tag.
	 * @return A {@link ReadyExecution} if the {@link StuckExecution} is now ready,
	 * <code>null</code> otherwise.  Returns <code>null</code> no matter what if this
	 * has already returned a {@link ReadyExecution}.
	 */
	ReadyExecution getReady(String name) {
		
		// already returned ready.
		if(ready == null) {
			return null;
		} else {
			ReadyExecution result = null;
			missingTags.removeElement(name);
			if(missingTags.size() == 0) {
				result = ready;
				ready = null;
			}
			return result;
		}
	}
}
