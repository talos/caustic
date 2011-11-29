package net.caustic.database;

import net.caustic.instruction.Instruction;

/**
 * An {@link Instruction} with a {@link #source} property, allowing easier restart.
 * @author talos
 *
 */
public class StoppedInstruction {
	
	public final Instruction instruction;
	public final String source;
	
	public StoppedInstruction(Instruction instruction, String source) {
		this.instruction = instruction;
		this.source = source;
	}
}
