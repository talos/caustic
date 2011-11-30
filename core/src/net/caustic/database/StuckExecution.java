package net.caustic.database;

import java.util.Vector;

import net.caustic.instruction.Instruction;
import net.caustic.util.VectorUtils;

class StuckExecution extends ReadyExecution {

	private final Vector missingTags = new Vector();
	
	StuckExecution(String source, Instruction instruction, String[] missingTags) {
		super(source, instruction);
		VectorUtils.arrayIntoVector(missingTags, this.missingTags);
	}
	
	/**
	 * Inform {@link StuckExecution} that a tag with name <code>name</code>
	 * has appeared in a scope that affects it.
	 * @param name The {@link String} name of the tag.
	 * @return <code>True</code> if the {@link StuckExecution} is no longer stuck;
	 * <code>false</code> otherwise.
	 */
	boolean found(String name) {
		missingTags.removeElement(name);
		if(missingTags.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
}
