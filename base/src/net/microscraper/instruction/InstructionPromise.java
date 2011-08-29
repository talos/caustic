package net.microscraper.instruction;

import net.microscraper.client.Deserializer;
import net.microscraper.database.Database;
import net.microscraper.util.Execution;

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
	private final Database database;
	private final String serializedString;
	private final String uri;
	
	/**
	 * Create a new {@link InstructionPromise}.
	 * @param deserializer The {@link Deserializer} to use.
	 * @param database
	 * @param serializedString The {@link String} containing the {@link Instruction} to be
	 * deserialized.
	 * @param uri The {@link String} URI to use when {@link Deserializer} resolves references
	 * in <code>serializedString</code>.
	 */
	public InstructionPromise(Deserializer deserializer, Database database, String serializedString, String uri) {
		this.deserializer = deserializer;
		this.database = database;
		this.serializedString = serializedString;
		this.uri = uri;
	}
	
	/**
	 * Load an {@link Instruction} using the information in {@link Variables}.
	 * @param sourceId The {@link int} source ID to use when extracting values
	 * from {@link Database}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is an
	 * {@link Instruction} if it is successful.
	 */
	public Execution load(int sourceId) {
		return deserializer.deserializeString(serializedString, database, sourceId, uri);
	}
	
	/**
	 * Returns {@link #serializedString}.
	 */
	public String toString() {
		return serializedString;
	}
}
