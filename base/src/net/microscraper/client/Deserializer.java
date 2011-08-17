package net.microscraper.client;

import java.io.IOException;

import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Load;
import net.microscraper.json.JsonObject;

/**
 * Interface to deserialize a {@link String} into a {@link Load} or {@link Find}
 * instruction.
 * @author talos
 *
 */
public interface Deserializer {

	/**
	 * Deserialize a {@link Load} from a {@link JsonObject}.
	 * @param serializedString {@link String} to deserialize.
	 * @return A {@link Load} instance.
	 * @throws DeserializationException If this is not a valid serialization of {@link Load}.
	 * @throws IOException If there is an error loading a reference inside the serialization.
	 */
	public abstract Load deserializeLoad(String serializedString)
		throws DeserializationException, IOException;

	/**
	 * Deserialize a {@link Find} from a {@link JsonObject}.
	 * @param serializedString {@link String} to deserialize.
	 * @return A {@link Find} instance.
	 * @throws DeserializationException If this is not a valid serialization of {@link Find}.
	 * @throws IOException If there is an error loading a reference inside the serialization.
	 */
	public abstract Find deserializeFind(String serializedString)
		throws DeserializationException, IOException;

}