package net.caustic.database;

import net.caustic.instruction.Instruction;

public class StuckExecution extends ReadyExecution {

	public final String[] missingTags;
	StuckExecution(String source, Instruction instruction, String[] missingTags) {
		super(source, instruction);
		this.missingTags = missingTags;
	}

}
