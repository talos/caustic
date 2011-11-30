package net.caustic.database;

import net.caustic.instruction.Instruction;

class FailedExecution extends ReadyExecution {

	final String failedBecause;
	
	FailedExecution(String source, Instruction instruction, String failedBecause) {
		super(source, instruction);
		this.failedBecause = failedBecause;
	}

}
