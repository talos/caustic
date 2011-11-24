package net.caustic.instruction;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.scope.Scope;

/**
 * This class places several instructions together to execute immediately from 
 * a single source and scope, as if they were one instruction.
 * @author talos
 *
 */
public class InstructionArray extends Instruction {
	
	private final Instruction[] instructions;
	
	public InstructionArray(Instruction[] instructions) {
		this.instructions = instructions;
	}
	
	public InstructionResult execute(String source, Database db, Scope scope,
			HttpBrowser browser) throws InterruptedException, DatabaseException {
		return InstructionResult.success(null, new String[] { source }, instructions, false);
	}

	public boolean shouldConfirm() {
		return false;
	}
}
