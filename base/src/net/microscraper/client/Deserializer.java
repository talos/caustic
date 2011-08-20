package net.microscraper.client;

import java.io.IOException;

import net.microscraper.instruction.Instruction;
import net.microscraper.json.JsonObject;

/**
 * Interface to deserialize a {@link String} into a {@link Load} or {@link Find}
 * instruction.
 * @author talos
 *
 */
public interface Deserializer {
	/**
	 * Deserialize an {@link Instruction} from a {@link JsonObject}.
	 * @param serializedString {@link String} to deserialize.
	 * @return A {@link Instruction} instance.
	 * @throws DeserializationException If this is not a valid serialization of {@link Find}.
	 * @throws IOException If there is an error loading a reference inside the serialization.
	 */
	public abstract Instruction deserializeInstruction(String serializedString)
		throws DeserializationException, IOException;

}