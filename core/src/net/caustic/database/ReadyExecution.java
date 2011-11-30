package net.caustic.database;

import net.caustic.instruction.Instruction;

/**
 * An {@link Instruction} with a {@link #source} property, allowing it to be started.
 * Think of it as a struct.
 * @author talos
 *
 */
class ReadyExecution {
	
	public final Instruction instruction;
	public final String source;
	
	ReadyExecution(String source, Instruction instruction) {
		this.instruction = instruction;
		this.source = source;
	}
}
