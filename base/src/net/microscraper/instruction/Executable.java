package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.database.Scope;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;

/**
 * An {@link Executable} is an execution-time binding of an {@link InstructionPromise} to
 * the data it needs to run.
 * @see Load
 * @see Find
 * @author talos
 *
 */
public class Executable {
	
	/**
	 * How many characters of {@link #source} to show in {@link #toString()}.
	 */
	public static final int SOURCE_TRUNCATE_LENGTH = 50;
	
	private final String source;
	private final Scope scope;
	private final InstructionPromise promise;
	
	/**
	 * The {@link Instruction} that this {@link Executable} runs.
	 */
	private Instruction instruction;
	private Execution prevExecution;
	private Execution execution;
	
	public Executable(String source, Scope scope, InstructionPromise instructionPromise) {
		this.source = source;
		this.scope = scope;
		this.promise = instructionPromise;
	}
	
	/**
	 * Runs {@link InstructionPromise#execute(Variables)} using the {@link #variables}
	 * to which this {@link Executable} is bound to get {@link #instruction}.<p>
	 * Once it has this {@link Instruction}, it performs
	 * {@link Instruction#execute(String, Variables)} using the {@link #source}
	 * and {@link #variables} that this {@link Executable} is bound to.
	 * @return An {@link Execution} object resulting from {@link Instruction#execute},
	 * whose {@link Execution#getExecuted()} is an array of {@link Executable}s.
	 * @throws InterruptedException If the user interrupts the execution.
	 * @throws IOException If there was an error persisting to {@link Database}.
	 */
	public Execution execute() throws InterruptedException, IOException {
		prevExecution = execution;
		
		// If instruction is not yet loaded, try to get it.
		if(instruction == null) {
			execution = promise.load(scope);
			if(execution.isSuccessful()) {
				instruction = (Instruction) execution.getExecuted();
			}
		}
		
		// If instruction is loaded, execute it.
		if(instruction != null) {
			execution = instruction.execute(source, scope);
		}
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
						combinedMissingVariables.length == prevExecution.getMissingVariables().length;
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
				+ "instructions " + StringUtils.quote(promise.toString());
			// +	" and variables " + StringUtils.quote(database.toString(sourceId));
	}
}
