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
	private final String uri;
	
	/**
	 * Create a new {@link InstructionPromise}.
	 * @param deserializer The {@link Deserializer} to use.
	 * @param serializedString The {@link String} containing the {@link Instruction} to be
	 * deserialized.
	 * @param uri The {@link String} URI to use when {@link Deserializer} resolves references
	 * in <code>serializedString</code>.
	 */
	public InstructionPromise(Deserializer deserializer, String serializedString, String uri) {
		this.deserializer = deserializer;
		this.serializedString = serializedString;
		this.uri = uri;
	}
	
	/**
	 * Load an {@link Instruction} using the information in {@link Variables}.
	 * @param variables The {@link Variables} to use in trying to obtain
	 * an {@link Instruction}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is an
	 * {@link Instruction} if it is successful.
	 */
	public Execution load(Variables variables) {
		return deserializer.deserializeString(serializedString, variables, uri);
	}
	
	/**
	 * Returns {@link #serializedString}.
	 */
	public String toString() {
		return serializedString;
	}
}
