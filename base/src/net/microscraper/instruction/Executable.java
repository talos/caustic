package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Deserializer;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;

/**
 * An {@link Executable} is binds an {@link InstructionPromise} or
 * {@link Instruction} to a {@link Scope} and {@link String} source,
 * for the purpose of repeatedly trying the {@link Instruction} with
 * an evolving set of data until it succeeds.
 * @see Instruction
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
	
	private SerializedInstruction serializedInstruction;
		
	/**
	 * The {@link Instruction} that this {@link Executable} runs.
	 */
	private Instruction instruction;
	private Execution prevExecution;
	private Execution execution;
	
	/**
	 * An {@link Executable} that has to load its {@link Instruction} using {@link Deserializer}.
	 */
	public Executable(String source, Scope scope, SerializedInstruction serializedInstruction) {
		this.source = source;
		this.scope = scope;
		this.serializedInstruction = serializedInstruction;
	}
	
	/**
	 * An {@link Executable} with an already-loaded {@link Instruction}.
	 * @param source
	 * @param scope
	 * @param instruction
	 */
	public Executable(String source, Scope scope, Instruction instruction) {
		this.source = source;
		this.scope = scope;
		this.instruction = instruction;
	}
	
	/**
	 * Runs {@link InstructionPromise#load(Scope)} using the {@link #scope}
	 * to which this {@link Executable} is bound to get {@link #instruction},
	 * unless {@link Executable} was created with an {@link Instruction}.<p>
	 * Once it has this {@link Instruction}, it performs
	 * {@link Instruction#execute(String, Scope)} using the {@link #source}
	 * and {@link #scope} that this {@link Executable} is bound to.
	 * @return An {@link Execution} object resulting from {@link Instruction#execute},
	 * whose {@link Execution#getExecuted()} is an array of {@link Executable}s.
	 * @throws InterruptedException If the user interrupts the execution.
	 * @throws IOException If there was an error persisting to {@link Database}.
	 */
	public Execution execute() throws InterruptedException, IOException {
		prevExecution = execution;
		
		// If instruction is not yet loaded, try to get it.
		if(instruction == null) {
			serializedInstruction.deserialize(source, scope);
			
			/*
			deserializer.deserialize(serializedString, database, scope, uri);
			execution = promise.load(scope);
			if(execution.isSuccessful()) {
				instruction = (Instruction) execution.getExecuted();
			}*/
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
	 * tags.
	 * @return <code>true</code> if the same tags are missing from
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
}
