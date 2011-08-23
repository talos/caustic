package net.microscraper.instruction;

import net.microscraper.client.Deserializer;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link InstructionPromise} allows for the lazy-loading of {@link Instruction}s.
 * It provides an interface for obtaining an {@link Execution} whose {@link Execution#getExecuted()}
 * is an {@link Instruction} from a {@link Variables} instance.
 * @author talos
 * @see {@link #load(Variables)}
 *
 */
public class InstructionPromise {
	
	private final Deserializer deserializer;
	private final String serializedString;
	private final String rootUri;
	
	public InstructionPromise(Deserializer deserializer, String serializedString, String rootUri) {
		this.deserializer = deserializer;
		this.serializedString = serializedString;
		this.rootUri = rootUri;
	}
	
	/**
	 * Load an {@link Instruction} using the information in {@link Variables}.
	 * @param variables The {@link Variables} to use in trying to obtain
	 * an {@link Instruction}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is an
	 * {@link Instruction} if it is successful.
	 */
	public Execution load(Variables variables) {
		return deserializer.deserializeString(serializedString, variables, rootUri);
	}
	
	/**
	 * Returns {@link #serializedString}.
	 */
	public String toString() {
		return serializedString;
	}
}
