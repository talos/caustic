package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
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
	
	public static int SOURCE_TRUNCATE_LENGTH = 50;
	
	private Execution prevExecution;
	private Execution execution;
	
	private final String source;
	private final Variables variables;
	private final Instruction instruction;
	
	public Executable(String source, Variables variables, Instruction instruction) {
		this.source = source;
		this.variables = variables;
		this.instruction = instruction;
	}
	
	/**
	 * Run {@link Instruction#execute(String, Variables)} using the {@link #source}
	 * and {@link #variables} that this {@link Executable} is bound to.
	 * @return An {@link Execution} object resulting from {@link Instruction#execute},
	 * whose {@link Execution#getExecuted()} is an array of {@link Executable}s.
	 * @throws InterruptedException If the user interrupts the execution.
	 * @throws IOException If there was an error persisting to {@link Database}.
	 */
	public Execution execute() throws InterruptedException, IOException {
		prevExecution = execution;
		execution = instruction.execute(source, variables);
		return execution;
	}
	
	/**
	 * Obtain the {@link Execution} object from the last call to {@link #execute()},
	 * without calling {@link #execute()} again.  Should not be called until
	 * {@link #execute()} has been called.
	 * @return The most recently obtained {@link Execution}.
	 */
	public Execution getLastExecution() {
		if(execution == null) {
			throw new IllegalStateException("Has not been executed.");
		}
		return execution;
	}
	
	/**
	 * Determine whether this {@link Executable} is stuck on the same missing
	 * {@link Variable}s.
	 * @return <code>true</code> if the same {@link Variables} are missing from
	 * the most recent execution and the one immediately before; <code>false</code>
	 * otherwise.
	 */
	public boolean isStuck() {
		if(execution != null && prevExecution != null) {
			if(execution.isMissingVariables() && prevExecution.isMissingVariables()) {
				String[] combinedMissingVariables
						= Execution.combine(new Execution[] { execution, prevExecution }).getMissingVariables();
				return combinedMissingVariables.length == execution.getMissingVariables().length &&
						combinedMissingVariables.length == getLastExecution().getMissingVariables().length;
			}
		}
		return false;
	}
	
	/**
	 * Provides information on the {@link #source}, {@link #instruction}, and {@link #variables}.
	 */
	public String toString() {
		return "Executable with " +
				(source != null ? " source " + StringUtils.truncate(StringUtils.quote(source), SOURCE_TRUNCATE_LENGTH) + " and " : "" )
				+ "instructions " + StringUtils.quote(instruction.toString()) +
				" and variables " + StringUtils.quote(variables.toString());
	}
}
