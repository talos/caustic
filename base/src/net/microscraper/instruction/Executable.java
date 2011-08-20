package net.microscraper.instruction;

import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} is an execution-time binding of an {@link Instruction} to
 * the data it needs to run.
 * @see Load
 * @see Find
 * @author talos
 *
 */
public class Executable {
	
	private Execution lastExecution;
	
	private final String source;
	private final Variables variables;
	private final Instruction instruction;
	
	public Executable(String source, Variables variables, Instruction instruction) {
		this.source = source;
		this.variables = variables;
		this.instruction = instruction;
	}
	
	public Execution execute() {
		lastExecution = instruction.execute(source, variables);
		return lastExecution;
	}
	
	public Execution lastExecution() {
		if(lastExecution == null) {
			throw new IllegalStateException("Has not been executed.");
		}
		return lastExecution;
	}
	
	public boolean isStuck() {
		// TODO
		return false;
	}
}
